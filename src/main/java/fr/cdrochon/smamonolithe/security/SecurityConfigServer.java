package fr.cdrochon.smamonolithe.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
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
                .csrf(Customizer.withDefaults())
                //                .csrf().disable()
                .cors(Customizer.withDefaults())
                .headers(h -> h.frameOptions(fo -> fo.disable()))
                .csrf(crsf -> crsf.ignoringAntMatchers("/h2-console"))
                .authorizeRequests(ar -> ar.antMatchers("/swagger-ui/**", "/v3/**").permitAll()
                                           // Toute autre requête nécessite une authentification
                                           .anyRequest().authenticated())
                // verifie le token envoyé par le client thymeleaf grace le certificat public defini dans le fichier application.properties
                .oauth2ResourceServer(o2 -> o2.jwt(token -> token.jwtAuthenticationConverter(jwtAuthConverter)))
                .build();
    }
}
