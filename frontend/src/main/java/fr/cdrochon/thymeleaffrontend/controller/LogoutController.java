package fr.cdrochon.thymeleaffrontend.controller;

import fr.cdrochon.thymeleaffrontend.logging.FrontendSecurityLoggers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class LogoutController {

    @Value("${app.security.logout-endpoint:http://localhost:8080/realms/sma-realm/protocol/openid-connect/logout}")
    private String keycloakLogoutEndpoint;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id:sma-thymeleaf-frontend}")
    private String keycloakClientId;

    @PostMapping("/se-deconnecter")
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) throws IOException {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        response.sendRedirect(buildLogoutTarget(request, authentication));
    }

    private String buildLogoutTarget(HttpServletRequest request, Authentication authentication) {
        String redirectUri = buildBaseUrl(request) + "/";
        StringBuilder target = new StringBuilder(keycloakLogoutEndpoint)
                .append("?post_logout_redirect_uri=")
                .append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));

        String idToken = extractIdToken(authentication);
        if (idToken != null && !idToken.isBlank()) {
            target.append("&id_token_hint=")
                    .append(URLEncoder.encode(idToken, StandardCharsets.UTF_8));
        } else {
            FrontendSecurityLoggers.security().warn(
                    "Impossible de recuperer l'id_token pour la deconnexion OIDC; fallback sur client_id.");
            target.append("&client_id=")
                    .append(URLEncoder.encode(keycloakClientId, StandardCharsets.UTF_8));
        }

        return target.toString();
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

