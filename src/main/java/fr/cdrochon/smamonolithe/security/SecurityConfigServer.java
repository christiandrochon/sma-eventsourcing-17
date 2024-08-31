package fr.cdrochon.smamonolithe.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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
     * @param httpSecurity
     * @return
     *
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(Customizer.withDefaults())
                //                .csrf().disable()
                .cors(Customizer.withDefaults())
                .headers(headers -> {})
                .headers(h -> h.frameOptions(fo -> fo.disable()))
                .csrf(crsf -> crsf.ignoringAntMatchers("/h2-console"))
//                .authorizeRequests(ar -> ar.antMatchers("/v3/**", "/", "/dossiers").permitAll()
               
   
//                .authorizeRequests(ar -> ar.antMatchers("/swagger-ui/**").permitAll())
//
//                // Configurer la gestion des sessions
////                .sessionManagement()
////                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
////                .and()
//                .authorizeRequests()
//                .antMatchers("/", "/index", "/templates/**", "/static/**", "/assets/**", "/garages").permitAll()


                // Configurer les autorisations des requêtes
                //                .authorizeRequests()
                //                // Exempter les ressources statiques de la sécurité
                //                .antMatchers(
                //                        "/assets/**",
                //                        "static/**",
                //                        "templates/**",
                //                        "/css/**",
                //                        "/js/**",
                //                        "/images/**",
                //                        "/webjars/**",
                //                        "/favicon.ico"
                //                            ).permitAll()
                //
                //                // Autoriser certaines URL publiques
                //                .antMatchers("/public/**").permitAll()

                // Toute autre requête nécessite une authentification
//                .anyRequest().authenticated())
                

//                .authorizeHttpRequests(ar -> ar.anyRequest().authenticated())
                // Exclure les ressources statiques
                //                .authorizeRequests(ar -> ar.antMatchers("/assets/**", "/css/**", "/js/**", "/images/**").permitAll())
                //                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                //                .authorizeHttpRequests(ar -> ar.requestMatchers("/h2-console/**").permitAll())
                //                .authorizeHttpRequests(ar -> ar.requestMatchers("/v3/**").permitAll())
                //                .authorizeHttpRequests(ar -> ar.requestMatchers("/swagger-ui/**").permitAll())
                //                .authorizeHttpRequests(ar -> ar.requestMatchers("/command/client/**").permitAll())
                //                .authorizeHttpRequests(ar->ar.requestMatchers("/garages").permitAll())
//                .oauth2Login(Customizer.withDefaults())
//                .logout(logout -> logout
//                        .logoutSuccessUrl("/").permitAll()
//                        .clearAuthentication(true)
//                        .deleteCookies("JSESSIONID"))

//                .oauth2Login(oauth2Login -> oauth2Login.loginPage("/oauth2Login"))
//                .oauth2Login(oauth2Login -> oauth2Login.defaultSuccessUrl("/"))
//                .oauth2ResourceServer(o2 -> o2.jwt(token -> token.jwtAuthenticationConverter(jwtAuthConverter)))
                .build();
    }
}
