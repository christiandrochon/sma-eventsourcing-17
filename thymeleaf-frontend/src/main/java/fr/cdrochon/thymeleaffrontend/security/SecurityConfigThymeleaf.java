package fr.cdrochon.thymeleaffrontend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfigThymeleaf {
    
    // Providers
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;
    
    //    public SecurityConfigThymeleaf(ClientRegistrationRepository clientRegistrationRepository) {
    //        this.clientRegistrationRepository = clientRegistrationRepository;
    //    }
    
    /**
     * Specifie les autorisations de connection. Prend en charge les attaques CSRF.
     * <p>
     * Sur le logout, je supprime le cookie JSESSIONID
     *
     * @param http
     * @return
     *
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // type MIME et ressources statiques
                .authorizeHttpRequests(ar -> ar.requestMatchers("/", "/assets/**", "/css/**", "/img/**", "/js/**", "templates/**").permitAll())
                .authorizeHttpRequests(ar -> ar.requestMatchers("/smalogin").permitAll())
                .authorizeHttpRequests(ar -> ar.requestMatchers("/auth").permitAll())
                .authorizeHttpRequests(ar -> ar.requestMatchers(HttpMethod.POST, "/logout").permitAll())
                .authorizeHttpRequests(ar -> ar.anyRequest().authenticated())
                //personnalisation de la page d'authentification
                .oauth2Login(login -> login.loginPage("/smalogin").defaultSuccessUrl("/dossiers"))
                // A la deconnection du provider, on retourne à la page d'accueil
                .logout(logout -> logout
                                .logoutSuccessHandler(oidcLogoutSuccessHandler())
                                .logoutSuccessUrl("/").permitAll()
                                .clearAuthentication(true)
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID")
                        //                        .deleteCookies("JSESSIONID", "MY_CUSTOM_CSRF_COOKIE", "AUTH_SESSION_ID_LEGACY", "AUTH_SESSION_ID")
                        //                        .addLogoutHandler((request, response, authentication) -> {
                        //                            Cookie[] cookies = request.getCookies();
                        //                            if(cookies != null) {
                        //                                for(Cookie cookie : cookies) {
                        //                                    if("JSESSIONID".equals(cookie.getName()) ||
                        //                                            "XSRF-TOKEN".equals(cookie.getName()) ||
                        //                                            "test".equals(cookie.getName()) ||
                        //                                            "testtest".equals(cookie.getName()) ||
                        //                                            "MY_CUSTOM_CSRF_COOKIE".equals(cookie.getName())) {
                        //                                        cookie.setMaxAge(0);
                        //                                        cookie.setPath("/");
                        //                                        response.addCookie(cookie);
                        //                                    }
                        //                                }
                        //                            }
                        //                        })
                       )
                //renvoi vers la page notAuthorized lorsque user n'as pas de droit.
                .exceptionHandling(eh ->
                                           eh.accessDeniedPage("/accesinterdit"))
                .build();
    }
    
    /**
     * Prise en charge de la deconnection, cad qu'on dit au provider connecté de se deconnecter et que l'appli doit faire une redicretion vers l'url par defaut
     *
     * @return objet qui prend en charge la deconnection
     */
    private OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
        final OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}?logoutsuccess=true");
        return oidcLogoutSuccessHandler;
    }
    
    
    /**
     * Recupere le token jwt de l'user qui s'est authentifié.
     * <p>
     * L'objet OAuth2AuthenticationToken suppose qu'on a fait l'authentification avec un provider qui supporte OpenID (keycloak ou google)
     * <p>
     * on doit importer la dependance oauth2-client pour la methode OAuth2AuhtenticationToken
     *
     * @return token jwt
     */
    public static String getJwtTokenValue() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        DefaultOidcUser oidcUser = (DefaultOidcUser) oAuth2AuthenticationToken.getPrincipal();
        String jwtTokenValue = oidcUser.getIdToken()
                                       .getTokenValue();
        return jwtTokenValue;
    }
    
    //    @Bean
    //public ClientRegistrationRepository clientRegistrationRepository() {
    //    ClientRegistration keycloakClient = ClientRegistration.withRegistrationId("keycloak")
    //        .clientId("thymeleaf-frontend")
    //        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
    //        .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
    //        .scope("openid", "profile", "email")
    //        .authorizationUri("http://keycloak:8080/realms/sma-realm/protocol/openid-connect/auth")
    //        .tokenUri("http://keycloak:8080/realms/sma-realm/protocol/openid-connect/token")
    //        .userInfoUri("http://keycloak:8080/realms/sma-realm/protocol/openid-connect/userinfo")
    //        .jwkSetUri("http://keycloak:8080/realms/sma-realm/protocol/openid-connect/certs")
    //        .build();
    //    return new InMemoryClientRegistrationRepository(keycloakClient);
    //}
    
    //    @Bean
    //    public ClientRegistrationRepository clientRegistrationRepository() {
    //        ClientRegistration keycloakClient = ClientRegistration.withRegistrationId("keycloak")
    //                                                              .clientId("thymeleaf-frontend")
    ////                                                              .clientSecret("your-client-secret")
    //                                                              .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
    //                                                              .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
    //                                                              .scope("openid", "profile", "email")
    //                                                              .authorizationUri("http://keycloak:8080/realms/sma-realm/protocol/openid-connect/auth")
    //                                                              .tokenUri("http://keycloak:8080/realms/sma-realm/protocol/openid-connect/token")
    //                                                              .userInfoUri("http://keycloak:8080/realms/sma-realm/protocol/openid-connect/userinfo")
    //                                                              .jwkSetUri("http://keycloak:8080/realms/sma-realm/protocol/openid-connect/certs")
    //                                                              .build();
    //        return new InMemoryClientRegistrationRepository(keycloakClient);
    //    }
}
