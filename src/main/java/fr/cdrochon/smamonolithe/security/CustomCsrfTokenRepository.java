package fr.cdrochon.smamonolithe.security;


import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

public class CustomCsrfTokenRepository implements CsrfTokenRepository {
    
    /**
     * Générer un token CSRF à chaque requete
     *
     * @param request requete HTTP
     * @return CsrfToken token CSRF
     */
    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        String token = UUID.randomUUID().toString();
        return new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", token);
    }
    
    /**
     * Sauvegarder le token CSRF dans un cookie à chaque requete
     *
     * @param csrfToken token CSRF
     * @param request   requete http
     * @param response  reponse http
     */
    @Override
    public void saveToken(CsrfToken csrfToken, HttpServletRequest request, HttpServletResponse response) {
        if (csrfToken == null) {
            Cookie cookie = new Cookie("X-CSRF-TOKEN", "");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
            System.out.println("CSRF token is null, setting empty cookie");
        } else {
            Cookie cookie = new Cookie("X-CSRF-TOKEN", csrfToken.getToken());
            cookie.setPath("/");
            cookie.setHttpOnly(false);
            cookie.setSecure(false); //pas de https
            cookie.setMaxAge(60);
            response.addHeader("Set-Cookie", cookie.getName() + "=" + cookie.getValue()
                    + "; Path=" + cookie.getPath()
                    + "; SameSite=None; HttpOnly"); // Ajout de SameSite=None
            response.addCookie(cookie);
            System.out.println("CSRF token set in cookie: " + csrfToken.getToken());
        }
    }
    
    /**
     * Charger le token CSRF à chaque requete
     * @param request requete http
     * @return CsrfToken token CSRF
     */
    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("X-CSRF-TOKEN".equals(cookie.getName())) {
                    return new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", cookie.getValue());
                }
            }
        }
        return null;
    }
    
}
