package fr.cdrochon.thymeleaffrontend.controller;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
    
    /**
     * Test de cookie pour csrf
     *
     * @param response
     * @return
     */
    @GetMapping("/test-csrf")
    public String testCsrf(HttpServletResponse response) {
        Cookie cookie = new Cookie("test", "testValue");
        cookie.setPath("/");
        cookie.setHttpOnly(false); // Dépend de votre configuration
        response.addCookie(cookie);
        return "test";
    }
}
