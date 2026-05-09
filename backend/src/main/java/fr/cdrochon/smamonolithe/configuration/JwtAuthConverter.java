package fr.cdrochon.smamonolithe.configuration;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Convertisseur JWT personnalisé pour extraire les rôles depuis realm_access.roles
 * et les mapper aux GrantedAuthority Spring Security avec le préfixe ROLE_.
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Récupère les autorités par défaut de Spring (scope, etc.)
        Collection<GrantedAuthority> authorities = Stream
                .concat(
                        // Autorités par défaut (scope)
                        jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                        // Autorités extraites de realm_access.roles
                        extractRealmRoles(jwt).stream()
                )
                .collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("sub"));
    }

    /**
     * Extrait les rôles depuis realm_access.roles du JWT
     * et les convertit en GrantedAuthority avec préfixe ROLE_
     */
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

        if (realmAccess == null || realmAccess.isEmpty()) {
            return Collections.emptySet();
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");

        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }
}

