package fr.cdrochon.thymeleaffrontend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
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
     * Personnalisation de la page d'authentification en affichant la liste des providers, mais avec la possibilité d'ajouter du css ou autre, dont images,
     * etc.
     *
     * @param model model de la page
     * @return page smalogin.html
     */
    @GetMapping("/smalogin")
    public String oauth2Login(Model model) {
        String authorizationRequestBaseUri = "oauth2/authorization";
        Map<String, String> oauth2AuthenticationUrls = new HashMap();
        Iterable<ClientRegistration> clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
        ;
        clientRegistrations.forEach(registration -> {
            oauth2AuthenticationUrls.put(registration.getClientName(),
                                         authorizationRequestBaseUri + "/" + registration.getRegistrationId());
        });
        model.addAttribute("urls", oauth2AuthenticationUrls);
        return "smalogin";
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
