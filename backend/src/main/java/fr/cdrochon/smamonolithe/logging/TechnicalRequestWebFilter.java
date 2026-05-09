package fr.cdrochon.smamonolithe.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class TechnicalRequestWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startMs = System.currentTimeMillis();
        String traceId = exchange.getRequest().getId();
        String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "UNKNOWN";
        String path = exchange.getRequest().getURI().getPath();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        return chain.filter(exchange)
                .doOnError(errorRef::set)
                .doFinally(signalType -> {
                    long durationMs = System.currentTimeMillis() - startMs;
                    HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
                    int status = statusCode != null ? statusCode.value() : (errorRef.get() != null ? 500 : 200);

                    if (errorRef.get() != null) {
                        log.error(
                                "TECH_HTTP traceId={} method={} path={} status={} durationMs={} signal={} error={}",
                                traceId, method, path, status, durationMs, signalType, errorRef.get().getMessage(), errorRef.get()
                        );
                    } else {
                        log.info(
                                "TECH_HTTP traceId={} method={} path={} status={} durationMs={} signal={}",
                                traceId, method, path, status, durationMs, signalType
                        );
                    }
                });
    }
}

