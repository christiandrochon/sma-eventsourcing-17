package fr.cdrochon.smamonolithe.logging;

import fr.cdrochon.smamonolithe.audit.domain.AuditAction;
import fr.cdrochon.smamonolithe.audit.domain.AuditEventRecord;
import fr.cdrochon.smamonolithe.audit.domain.AuditResult;
import fr.cdrochon.smamonolithe.audit.infrastructure.AuditPathResolver;
import fr.cdrochon.smamonolithe.audit.infrastructure.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Filtre WebFlux central : log technique + audit RGPD sur chaque requête HTTP.
 *
 * <p>Responsabilités :</p>
 * <ul>
 *   <li>Log technique (fichier technical.log)</li>
 *   <li>Log sécurité (fichier security.log) pour anomalies et accès refusés</li>
 *   <li>Enregistrement d'un événement d'audit dans la base PostgreSQL {@code audit} via {@link AuditService}</li>
 * </ul>
 *
 * <p>Si un JWT Keycloak est present, l'acteur et son garage sont extraits automatiquement.
 * Sinon, l'acteur reste {@code ANONYMOUS}.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class TechnicalRequestWebFilter implements WebFilter {

    private final AuditService auditService;

    public TechnicalRequestWebFilter(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return resolveActorContext(exchange)
                .flatMap(actorContext -> doFilter(exchange, chain, actorContext));
    }

    private Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, ActorContext actorContext) {
        long startMs = System.currentTimeMillis();
        String traceId = exchange.getRequest().getId();
        String method  = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "UNKNOWN";
        String path    = exchange.getRequest().getURI().getPath();
        String ip      = resolveIp(exchange);
        String agent   = exchange.getRequest().getHeaders().getFirst("User-Agent");

        boolean suspiciousMethod = !"GET".equals(method) && !"POST".equals(method)
                && !"HEAD".equals(method) && !"OPTIONS".equals(method);
        boolean suspiciousPath = path.contains("..") || path.contains("//")
                || path.contains("%2e") || path.contains("%2f") || path.contains("\\");

        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        // ---- Log sécurité + audit ANOMALIE ----
        if (suspiciousMethod || suspiciousPath) {
            BackendSecurityLoggers.security().warn(
                    "SEC_HTTP_ANOMALOUS_REQUEST traceId={} method={} path={} suspiciousMethod={} suspiciousPath={}",
                    traceId, method, path, suspiciousMethod, suspiciousPath);

            auditService.record(AuditEventRecord.builder()
                    .actor(actorContext.actor())
                    .actorGarage(actorContext.actorGarage())
                    .action(AuditAction.ANOMALY)
                    .resource(AuditPathResolver.resolveResource(path))
                    .resourceId(AuditPathResolver.resolveResourceId(path))
                    .result(AuditResult.DENIED)
                    .httpMethod(method)
                    .httpPath(path)
                    .ipAddress(ip)
                    .userAgent(agent)
                    .details("suspiciousMethod=" + suspiciousMethod + " suspiciousPath=" + suspiciousPath)
                    .build());
        }

        return chain.filter(exchange)
                .doOnError(errorRef::set)
                .doFinally(signalType -> {
                    long durationMs = System.currentTimeMillis() - startMs;
                    HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
                    int status = statusCode != null ? statusCode.value() : (errorRef.get() != null ? 500 : 200);

                    // ---- Log sécurité + audit ACCES REFUSE ----
                    if (status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value()) {
                        BackendSecurityLoggers.security().warn(
                                "SEC_HTTP_ACCESS_DENIED traceId={} method={} path={} status={} durationMs={}",
                                traceId, method, path, status, durationMs);

                        auditService.record(AuditEventRecord.builder()
                                .actor(actorContext.actor())
                                .actorGarage(actorContext.actorGarage())
                                .action(AuditAction.ACCESS_DENIED)
                                .resource(AuditPathResolver.resolveResource(path))
                                .resourceId(AuditPathResolver.resolveResourceId(path))
                                .result(AuditResult.DENIED)
                                .httpMethod(method)
                                .httpPath(path)
                                .httpStatus(status)
                                .ipAddress(ip)
                                .userAgent(agent)
                                .build());
                    } else {
                        // ---- Audit normal : toute requête réussie ou en erreur ----
                        AuditResult auditResult = errorRef.get() != null ? AuditResult.ERROR
                                : (status >= 400 ? AuditResult.ERROR : AuditResult.SUCCESS);

                        auditService.record(AuditEventRecord.builder()
                                .actor(actorContext.actor())
                                .actorGarage(actorContext.actorGarage())
                                .action(AuditPathResolver.resolveAction(method))
                                .resource(AuditPathResolver.resolveResource(path))
                                .resourceId(AuditPathResolver.resolveResourceId(path))
                                .result(auditResult)
                                .httpMethod(method)
                                .httpPath(path)
                                .httpStatus(status)
                                .ipAddress(ip)
                                .userAgent(agent)
                                .details(errorRef.get() != null ? errorRef.get().getMessage() : null)
                                .build());
                    }

                    // ---- Log technique ----
                    if (errorRef.get() != null) {
                        log.error("TECH_HTTP traceId={} method={} path={} status={} durationMs={} signal={} error={}",
                                traceId, method, path, status, durationMs, signalType,
                                errorRef.get().getMessage(), errorRef.get());
                    } else {
                        log.info("TECH_HTTP traceId={} method={} path={} status={} durationMs={} signal={}",
                                traceId, method, path, status, durationMs, signalType);
                    }
                });
    }

    private Mono<ActorContext> resolveActorContext(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .map(this::toActorContext)
                .defaultIfEmpty(ActorContext.anonymous())
                .onErrorReturn(ActorContext.anonymous());
    }

    private ActorContext toActorContext(Principal principal) {
        if (!(principal instanceof Authentication authentication)) {
            String name = principal != null && principal.getName() != null && !principal.getName().isBlank()
                    ? principal.getName()
                    : "ANONYMOUS";
            return new ActorContext(name, null);
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt jwt = jwtAuthenticationToken.getToken();
            String actor = firstNonBlank(
                    jwt.getClaimAsString("preferred_username"),
                    jwt.getClaimAsString("email"),
                    jwt.getClaimAsString("sub"),
                    authentication.getName(),
                    "ANONYMOUS"
            );
            String actorGarage = firstNonBlank(
                    jwt.getClaimAsString("garage_id"),
                    jwt.getClaimAsString("garage"),
                    jwt.getClaimAsString("garageId"),
                    null
            );
            return new ActorContext(actor, actorGarage);
        }

        String actor = firstNonBlank(authentication.getName(), "ANONYMOUS");
        return new ActorContext(actor, null);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------

    private static String resolveIp(ServerWebExchange exchange) {
        // En cas de reverse proxy, tenter X-Forwarded-For
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        InetSocketAddress remoteAddr = exchange.getRequest().getRemoteAddress();
        return remoteAddr != null ? remoteAddr.getAddress().getHostAddress() : "unknown";
    }

    private record ActorContext(String actor, String actorGarage) {
        static ActorContext anonymous() {
            return new ActorContext("ANONYMOUS", null);
        }
    }
}

