package fr.cdrochon.smamonolithe.vehicule.controller;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Securite du ms
 *
 * Transmission du JWT du user d'un ms vers un autre
 *
 * Securisation de l'application en fournissant un jwt aux microservices à chaque requete avec OpenFeign
 * Pour chaque requete envoyée depuis le service vehiculeservice, transfere le jwt (recu de angular)
 * <p>
 * A chaque fois que le service vehiculeservice envoi une requete , l'intercepteur intercepte la requete.
 * On recupere le contexte de securité de spring securité pour recuperer l'objet authentication.
 * Grace à authentication (qui est de type oauth), je recupere le jwt que je transfere dans header de la
 * requete qui va etre translatée en lui specifiant que le header est de type "Authorization : Bearer "
 * avec le jwt
 */
@Component
public class FeignInterceptor implements RequestInterceptor {
    @Override

    public void apply(RequestTemplate requestTemplate) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        String jwtAccessToken = jwtAuthenticationToken.getToken()
                                                      .getTokenValue();
        requestTemplate.header("Authorization", "Bearer " + jwtAccessToken);
    }
}
