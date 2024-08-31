package fr.cdrochon.thymeleaffrontend.security;

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
        return http
                .csrf(Customizer.withDefaults())
                //                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .cors(Customizer.withDefaults())
                .headers(h -> h.frameOptions(fo -> fo.disable()))
                .headers(headers -> {
                })
                .authorizeHttpRequests(ar -> ar.requestMatchers("/").permitAll())
                // type MIME et ressources statiques
                .authorizeHttpRequests(ar -> ar.requestMatchers("/assets/**", "/css/**", "/img/**", "/js/**", "templates/**").permitAll())
                .authorizeHttpRequests(ar -> ar.requestMatchers("/auth", "/smalogin").permitAll())
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
                .oauth2Login(login -> login.loginPage("/smalogin").defaultSuccessUrl("/"))
                //                .oauth2ResourceServer(o2 -> o2.jwt(token -> token.jwtAuthenticationConverter(jwtAuthConverter)))
                //
                //                //                .oauth2Login(oauth2Login -> oauth2Login.loginPage("/oauth2Login"))
                // A la deconnection du provider, on retourne à la page d'accueil
                .logout(logout -> logout
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                        .logoutSuccessUrl("/").permitAll()
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))
                //
                //                //                .oauth2Login(al ->
                //                //                                     al.loginPage("/oauth2Login")
                //                //                                       .defaultSuccessUrl("/")
                //                            )
                
                //renvoi vers la page notAuthorized lorsque user n'as pas de droit.
                .exceptionHandling(eh -> eh.accessDeniedPage("/accesinterdit"))
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
