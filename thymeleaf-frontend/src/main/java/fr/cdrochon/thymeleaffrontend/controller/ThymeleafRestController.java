package fr.cdrochon.thymeleaffrontend.controller;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class ThymeleafRestController {
    
    private final ClientRegistrationRepository clientRegistrationRepository;
    
    public ThymeleafRestController(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }
    
    /**
     * Renvoi les informations sur la session courante et l'utilisateur authentifié
     *
     * @param authentication authentication
     * @return objet authentication (au format json avec @ResponseBody)
     */
    @GetMapping("/auth")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseBody
    public Authentication authentication(Authentication authentication) {
        return authentication;
    }
    
    /**
     * Par défaut, on retourne la page index.html
     *
     * @return page index.html
     */
    @GetMapping("/")
    @PreAuthorize("permitAll()")
    public String index() {
        
        return "index";
    }
    
    /**
     * Renvoi l'user vers la page accesinterdit.html lorsqu'il tente de se rendre sur une url du site dont il n'a pas les droits
     *
     * @return page accesinterdit.html
     */
    @GetMapping("/accesinterdit")
    public String nonAutorise() {
        return "accesinterdit";
    }
    
    /**
     * Renvoi vers une page d'authentification personnalisée affichant la liste des providers oauth2 disponibles
     *
     * @param model model de la page
     * @return page smalogin.html
     */
    @GetMapping("/smalogin")
    public String oauth2Login(Model model) {
        // toutes les urls commencent par oauth2/authorization
        String authorizationRequestBaseUri = "oauth2/authorization/";
        Map<String, String> oauth2AuthenticationUrls = new HashMap();
        Iterable<ClientRegistration> clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
        ;
        clientRegistrations.forEach(registration -> {
            oauth2AuthenticationUrls.put(registration.getClientName(),
                                         authorizationRequestBaseUri + registration.getRegistrationId());
        });
        model.addAttribute("urls", oauth2AuthenticationUrls);
        log.info("oauth2AuthenticationUrls = " + oauth2AuthenticationUrls);
        
        return "smalogin";
    }
    
    @GetMapping("/test-csrf")
    public String testCsrf(HttpServletResponse response) {
        Cookie cookie = new Cookie("test", "testValue");
        cookie.setPath("/");
        cookie.setHttpOnly(false); // Dépend de votre configuration
        response.addCookie(cookie);
        return "test";
    }
    
    
    /**
     * Recupere le token jwt de l'user qui s'est authentifié
     * <p>
     * L'objet OAuth2AuthenticationToken suppose qu'on a fait l'authentification avec un provider qui supporte OpenID (keycloak ou google)
     * <p>
     * on doit importer la dependance oauth2-client pour la methode OAuth2AuhtenticationToken
     *
     * @return token jwt
     */
    private String getJwtTokenValue() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        DefaultOidcUser oidcUser = (DefaultOidcUser) oAuth2AuthenticationToken.getPrincipal();
        String jwtTokenValue = oidcUser.getIdToken()
                                       .getTokenValue();
        return jwtTokenValue;
    }
}
