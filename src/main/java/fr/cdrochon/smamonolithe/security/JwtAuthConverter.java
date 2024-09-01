package fr.cdrochon.smamonolithe.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Recupere les attributs d'un jwt, et mappe les roles à l'user authentifié, ce qui permet de delivrer les données à une application frontend
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    
    /**
     * Convertit un jwt en un objet d'authentification
     *
     * @param jwt jwt à convertir
     * @return objet d'authentification
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractResourceRoles(jwt).stream()
                                                                ).collect(Collectors.toSet());
        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaim("preferred_username"));
    }
    
    /**
     * Recupere les roles d'un user depuis son jwt grace à la clé 'realm_access'
     *
     * @param jwt jwt à convertir
     * @return roles d'un user
     */
    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> realmAccess;
        Collection<String> roles;
        if(jwt.getClaim("realm_access") == null) {
            return new HashSet<>();
        }
        realmAccess = jwt.getClaim("realm_access");
        roles = (Collection<String>) realmAccess.get("roles");
        return roles.stream().map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toSet());
    }
    
}
