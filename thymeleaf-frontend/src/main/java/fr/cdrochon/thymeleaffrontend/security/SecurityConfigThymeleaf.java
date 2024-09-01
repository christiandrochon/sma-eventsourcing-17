package fr.cdrochon.thymeleaffrontend.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import java.util.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfigThymeleaf {
    
    // Providers
    private final ClientRegistrationRepository clientRegistrationRepository;
    
    public SecurityConfigThymeleaf(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }
    
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
        CookieCsrfTokenRepository csrfTokenRepository = new CookieCsrfTokenRepository();
        //        csrfTokenRepository.setCookieName("XSRF-TOKEN"); // Nom du cookie
        csrfTokenRepository.setCookieName("MY_CUSTOM_CSRF_COOKIE");
        csrfTokenRepository.setParameterName("test");
        csrfTokenRepository.setCookieHttpOnly(true);  // HttpOnly pour la sécurité
        //        csrfTokenRepository.setCookieSecure(true);    // Secure pour HTTPS
        csrfTokenRepository.setCookiePath("/");       // Chemin du cookie
        //        csrfTokenRepository.setCookieSameSite("Strict");  // SameSite pour limiter les envois inter-domaines
        
        HttpSessionCsrfTokenRepository hfe = new HttpSessionCsrfTokenRepository();
        hfe.setHeaderName("MY-CSRF-SESSION-TOKEN");
        hfe.setParameterName("MY-CSRF-PARAM-TOKEN");
        hfe.setSessionAttributeName("MY-CSRF-SESSION-ATTR");
        
        return http
//                                .csrf(Customizer.withDefaults())
                //                .csrf(csrf -> csrf.disable())
                //                .csrf(csrf -> csrf.ignoringRequestMatchers("/smalogin").csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                // exclue la page de login de la protection CSRF
//                                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())) //stocke le jeton dans un cookie
                                .csrf(csrf -> csrf.csrfTokenRepository(new HttpSessionCsrfTokenRepository())) //stoke le jeton dans la session
//                .csrf(csrf -> csrf.csrfTokenRepository(hfe)) //stoke le jeton dans la session
//                                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
                .cors(Customizer.withDefaults())
                .headers(h -> {
                    h.frameOptions(fo -> fo.disable());
                    //                    h.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self' http://sma.local; " +
                    //                                                                       "script-src 'self' 'unsafe-inline' http://sma.local; " +
                    //                                                                       "script-src-elem 'self' 'unsafe-inline' http://sma.local; " +
                    //                                                                       "script-src-attr 'self' 'unsafe-inline' http://sma.local; " +
                    //                                                                       "style-src 'self' 'unsafe-inline' http://sma.local; " +
                    //                                                                       "img-src 'self' data: http://sma.local; " +
                    //                                                                       "media-src 'self' data: http://sma.local; " +
                    //                                                                       "font-src 'self' http://sma.local; " +
                    //                                                                       "connect-src 'self' http://sma.local; " +
                    //                                                                       "frame-src 'self' http://sma.local; " +
                    //                                                                       "object-src 'self' http://sma.local; " +
                    //                                                                       "child-src 'self' http://sma.local; " +
                    //                                                                       "form-action 'self' http://sma.local;")
                    //                    );
                    
                })
                
                .authorizeHttpRequests(ar -> ar.requestMatchers("/").permitAll())
                // type MIME et ressources statiques
                .authorizeHttpRequests(ar -> ar.requestMatchers("/", "/assets/**", "/css/**", "/img/**", "/js/**", "templates/**").permitAll())
                .authorizeHttpRequests(ar -> ar.requestMatchers("/smalogin").permitAll())
                .authorizeHttpRequests(ar -> ar.requestMatchers("/auth").permitAll())
                                .authorizeHttpRequests(ar -> ar.requestMatchers(HttpMethod.POST, "/logout").permitAll())
                .authorizeHttpRequests(ar -> ar.requestMatchers(HttpMethod.POST, "/commands/createDocument").hasAnyAuthority("USER", "ADMIN"))
                //                .authorizeHttpRequests(ar -> ar.requestMatchers("/", "/index", "/oauth2Login/**", "/webjars/**", "/h2-console/**")
                //                .permitAll())
                //                .authorizeHttpRequests(ar -> ar.requestMatchers(HttpMethod.GET, "/queries/**").permitAll())
                //                //                .authorizeHttpRequests(ar->ar.requestMatchers("/commands/**").permitAll())
                //                .authorizeHttpRequests(ar -> ar.requestMatchers(HttpMethod.POST, "/commands/**"))
                .authorizeHttpRequests(ar -> ar.anyRequest().authenticated())
                // authentification avec oauth2
                //personnalisation de la page d'authentification
                //                .oauth2Login(Customizer.withDefaults())
                .oauth2Login(login -> login.loginPage("/smalogin").defaultSuccessUrl("/dossiers"))
                //                .oauth2ResourceServer(o2 -> o2.jwt(token -> token.jwtAuthenticationConverter(jwtAuthConverter)))
                //
                //                //                .oauth2Login(oauth2Login -> oauth2Login.loginPage("/oauth2Login"))
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
                //
                //                //                .oauth2Login(al ->
                //                //                                     al.loginPage("/oauth2Login")
                //                //                                       .defaultSuccessUrl("/")
                //                            )
                
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
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}?logoutsuccess=true");
        return oidcLogoutSuccessHandler;
    }
    
}
