package fr.cdrochon.smamonolithe.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Convertit un JWT Keycloak en Authentication Spring Security (reactive).
 *
 * Roles pris en charge:
 * - realm_access.roles -> ROLE_*
 * - resource_access.<client>.roles -> ROLE_*
 * - scopes OAuth2 standards -> SCOPE_*
 */
@Component
public class KeycloakReactiveJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        Collection<GrantedAuthority> scopes = scopeConverter.convert(jwt);
        if (scopes != null) {
            authorities.addAll(scopes);
        }

        authorities.addAll(extractRealmRoles(jwt));
        authorities.addAll(extractClientRoles(jwt));

        String principalName = firstNonBlank(
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("sub"),
                "anonymous"
        );

        return Mono.just(new JwtAuthenticationToken(jwt, authorities, principalName));
    }

    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Object realmAccessObj = jwt.getClaims().get("realm_access");
        if (!(realmAccessObj instanceof Map<?, ?> realmAccess)) {
            return Set.of();
        }

        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof Collection<?> roles)) {
            return Set.of();
        }

        Set<GrantedAuthority> mapped = new LinkedHashSet<>();
        for (Object roleObj : roles) {
            if (roleObj != null) {
                String role = roleObj.toString().trim();
                if (!role.isEmpty()) {
                    mapped.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }
        }
        return mapped;
    }

    private Collection<GrantedAuthority> extractClientRoles(Jwt jwt) {
        Object resourceAccessObj = jwt.getClaims().get("resource_access");
        if (!(resourceAccessObj instanceof Map<?, ?> resourceAccess)) {
            return Set.of();
        }

        Set<GrantedAuthority> mapped = new LinkedHashSet<>();

        for (Object clientEntryObj : resourceAccess.values()) {
            if (!(clientEntryObj instanceof Map<?, ?> clientMap)) {
                continue;
            }
            Object rolesObj = clientMap.get("roles");
            if (!(rolesObj instanceof Collection<?> roles)) {
                continue;
            }
            for (Object roleObj : roles) {
                if (roleObj != null) {
                    String role = roleObj.toString().trim();
                    if (!role.isEmpty()) {
                        mapped.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }
            }
        }

        return mapped;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}

