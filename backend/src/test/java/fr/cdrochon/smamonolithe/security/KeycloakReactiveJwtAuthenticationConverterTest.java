package fr.cdrochon.smamonolithe.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakReactiveJwtAuthenticationConverterTest {

    private final KeycloakReactiveJwtAuthenticationConverter converter = new KeycloakReactiveJwtAuthenticationConverter();

    @Test
    void shouldMapRealmAndClientRolesAndPrincipal() {
        Jwt jwt = jwtWithClaims(Map.of(
                "preferred_username", "auditeur.externe",
                "realm_access", Map.of("roles", List.of("ADMIN", "AUDITOR")),
                "resource_access", Map.of(
                        "sma-monolithe", Map.of("roles", List.of("USER", "AUDITOR"))
                ),
                "scope", "openid profile"
        ));

        AbstractAuthenticationToken token = converter.convert(jwt).block();
        Set<String> authorities = token.getAuthorities().stream()
                .map(granted -> granted.getAuthority())
                .collect(Collectors.toSet());

        assertEquals("auditeur.externe", token.getName());
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("ROLE_AUDITOR"));
        assertTrue(authorities.contains("ROLE_USER"));
        assertTrue(authorities.contains("SCOPE_openid"));
        assertTrue(authorities.contains("SCOPE_profile"));
    }

    @Test
    void shouldFallbackToSubjectWhenNoPreferredUsername() {
        Jwt jwt = jwtWithClaims(Map.of(
                "sub", "d9f1a5fe-7b7d-470b-9ee1-1f6d9d42b7f2",
                "realm_access", Map.of("roles", List.of("USER"))
        ));

        AbstractAuthenticationToken token = converter.convert(jwt).block();
        assertEquals("d9f1a5fe-7b7d-470b-9ee1-1f6d9d42b7f2", token.getName());
    }

    private static Jwt jwtWithClaims(Map<String, Object> claims) {
        Instant now = Instant.now();
        return new Jwt("token-value", now.minusSeconds(5), now.plusSeconds(3600),
                Map.of("alg", "none"), claims);
    }
}

