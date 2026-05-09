package fr.cdrochon.thymeleaffrontend.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "true")
public class SecurityReactiveConfig {

    @Value("${app.security.logout-endpoint:http://localhost:8080/realms/sma-realm/protocol/openid-connect/logout}")
    private String keycloakLogoutEndpoint;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id:sma-thymeleaf-frontend}")
    private String keycloakClientId;

    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            String redirectUri = buildBaseUrl(request) + "/";
            StringBuilder target = new StringBuilder(keycloakLogoutEndpoint)
                    .append("?post_logout_redirect_uri=")
                    .append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));

            String idToken = extractIdToken(authentication);
            if (idToken != null && !idToken.isBlank()) {
                target.append("&id_token_hint=")
                        .append(URLEncoder.encode(idToken, StandardCharsets.UTF_8));
            } else {
                // Fallback utile si l'ID token n'est pas present dans le principal.
                target.append("&client_id=")
                        .append(URLEncoder.encode(keycloakClientId, StandardCharsets.UTF_8));
            }

            response.sendRedirect(target.toString());
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   LogoutSuccessHandler oidcLogoutSuccessHandler) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index").permitAll()
                        .requestMatchers("/actuator/health", "/error", "/favicon.ico").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/img/**", "/assets/**", "/webjars/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler))
                .build();
    }

    private static String buildBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return defaultPort ? scheme + "://" + host : scheme + "://" + host + ":" + port;
    }

    private static String extractIdToken(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getIdToken().getTokenValue();
        }
        return null;
    }
}
