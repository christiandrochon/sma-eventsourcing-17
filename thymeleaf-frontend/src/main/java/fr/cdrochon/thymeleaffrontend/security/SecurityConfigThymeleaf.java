package fr.cdrochon.thymeleaffrontend.security;

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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfigThymeleaf {
    
    // objet qui enregistre les differents providers
    private ClientRegistrationRepository clientRegistrationRepository;
    private final JwtAuthConverter jwtAuthConverter;
    
    public SecurityConfigThymeleaf(ClientRegistrationRepository clientRegistrationRepository,
                                   JwtAuthConverter jwtAuthConverter) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.jwtAuthConverter = jwtAuthConverter;
    }
    
    /**
     * Specifie les autorisations de connection. Prend en charge les attaques CSRF.
     * <p>
     * Sur le logout, je supprime le cookie JSESSIONID
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(Customizer.withDefaults())
                //                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .cors(Customizer.withDefaults())
                .headers(h -> h.frameOptions(fo -> fo.disable()))
                
                .authorizeHttpRequests(ar -> ar.requestMatchers("/", "/oauth2Login/**", "/webjars/**", "/h2-console/**").permitAll())
                .authorizeHttpRequests(ar -> ar.requestMatchers(HttpMethod.GET, "/queries/**").permitAll())
                //                .authorizeHttpRequests(ar->ar.requestMatchers("/commands/**").permitAll())
                .authorizeHttpRequests(ar -> ar.requestMatchers(HttpMethod.POST, "/commands/**"))
                .authorizeHttpRequests(ar -> ar.anyRequest().authenticated())
                
                .oauth2ResourceServer(o2 -> o2.jwt(token -> token.jwtAuthenticationConverter(jwtAuthConverter)))
                //personnalisation de la page d'authentification
                //                .oauth2Login(Customizer.withDefaults())
                .oauth2Login(al ->
                                     al.loginPage("/oauth2Login")
                                       .defaultSuccessUrl("/")
                            )
                .logout((logout) -> logout
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                        .logoutSuccessUrl("/").permitAll()
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))
                
                //renvoi vers la page notAuthorized lorsque user n'as pas de droit.
                .exceptionHandling(eh -> eh.accessDeniedPage("/notAutorized"))
                .build();
    }
    
    /**
     * Prise en charge de la deconnection, cad qu'on dit au provider connecté de se deconnecter et que l'appli doit
     * faire une redicretion vers l'url par defaut
     *
     * @return objet qui prend en charge la deconnection
     */
    private OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
        final OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}?logoutsuccess=true");
        return oidcLogoutSuccessHandler;
    }
    
    /**
     * Mapper qui permet de mapper les roles recus dans un jwt et les recuperer dans l'appli. On est obligé de faire
     * ca car le format des jwt n'est jamais le meme en fonction des providers.
     *
     * @return liste de roles d'un user
     */
    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            final Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            authorities.forEach((authority) -> {
                if(authority instanceof OidcUserAuthority oidcAuth) {
                    mappedAuthorities.addAll(mapAuthorities(oidcAuth.getIdToken().getClaims()));
                    System.out.println(oidcAuth.getAttributes());
                }
                else if(authority instanceof OAuth2UserAuthority oauth2Auth) {
                    mappedAuthorities.addAll(mapAuthorities(oauth2Auth.getAttributes()));
                }
            });
            return mappedAuthorities;
        };
    }
    
    
    /**
     * Fournit la liste des providers qui permettent de se connecter à l'application frontend.
     *
     * @param attributes
     * @return
     */
    private List<SimpleGrantedAuthority> mapAuthorities(final Map<String, Object> attributes) {
        final Map<String, Object> realmAccess = ((Map<String, Object>) attributes.getOrDefault("realm_access", Collections.emptyMap()));
        final Collection<String> roles = ((Collection<String>) realmAccess.getOrDefault("roles", Collections.emptyList()));
        return roles.stream()
                    .map((role) -> new SimpleGrantedAuthority(role))
                    .toList();
    }
    
    /**
     * //     * Politique de CORS origin par Spring Security, servant à emettre des requetes
     * //     * @return
     * //
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        //        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8092"));
        //        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8081"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
