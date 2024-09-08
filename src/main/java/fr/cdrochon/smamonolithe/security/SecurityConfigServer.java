package fr.cdrochon.smamonolithe.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
//@KeycloakConfiguration
public class SecurityConfigServer {
    
    private final JwtAuthConverter jwtAuthConverter;
    
    public SecurityConfigServer(JwtAuthConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }
    
    /**
     * Spring security supprime les frames ar defaut, car considere que c'est une faille de securité
     * <p>
     * Recuperation des roles depuis le jwt
     * <p>Attention, utilisation de authorizeHttpRequests qui devrait etre compatible uniquement avec java 17. Si erreur, utiliser la methode
     * authorizeRequests. Les mises à jour pourront etre problematiques.
     * </p>
     *
     * @param httpSecurity httpSecurity à configurer
     * @return SecurityFilterChain
     *
     * @throws Exception exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                //                                .csrf(Customizer.withDefaults())
                //                .csrf().disable()
                //                                .cors(Customizer.withDefaults())
                
                //************************ CSRF ************************
                //        JWT dans les en-têtes HTTP : Les JWT sont envoyés dans les en-têtes HTTP, ce qui les protège contre les attaques CSRF.
                //                Pas de session basée sur des cookies : Si vous n'utilisez pas de sessions basées sur des cookies, les tokens CSRF ne sont
                //                pas nécessaires.
                //        Keycloak : Keycloak gère l'authentification et l'autorisation, et les JWT sont utilisés pour sécuriser les API.
                // .csrf(csrf -> csrf.csrfTokenRepository(new CustomCsrfTokenRepository()))
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeRequests(ar -> {
                    ar.antMatchers("/actuator/**", "/public/**", "/swagger-ui/**", "/v3/**").permitAll()
                      // Toute autre requête nécessite une authentification
                      .anyRequest().authenticated();
                })
                //                        .anyRequest().permitAll())
                // verifie le token envoyé par le client thymeleaf grace au certificat public defini dans le fichier application.properties
                .oauth2ResourceServer(o2 -> o2.jwt(token -> token.jwtAuthenticationConverter(jwtAuthConverter)))
                
                .build();
    }
    
    //    @Bean
    //    public JwtDecoder jwtDecoder() {
    //        String issuerUri = "http://localhost:8080/realms/sma-realm"; // Replace with your issuer URI
    //        return NimbusJwtDecoder.withJwkSetUri(issuerUri + "/protocol/openid-connect/certs").build();
    //    }
}
