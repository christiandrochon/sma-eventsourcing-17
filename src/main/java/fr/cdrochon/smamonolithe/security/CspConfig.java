package fr.cdrochon.smamonolithe.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration

public class CspConfig {
    
//    @Bean
    public SecurityFilterChain securityFilterChainCsp(HttpSecurity http) throws Exception {
        http
                // desactive la CSP
                //                .headers(headers -> {})
                // active la CSP
                .headers(headers -> headers
                                 .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives("default-src 'self' http://sma.local; http://localhost; " +
                                                                                          "script-src 'self' 'unsafe-inline' http://sma.local; " +
                                                                                          "http://localhost; " +
                                                                                          "script-src-elem 'self' 'unsafe-inline' http://sma.local; http://localhost; " +
                                                                                          "script-src-attr 'self' 'unsafe-inline' http://sma.local; http://localhost; " +
                                                                                          "style-src 'self' 'unsafe-inline' http://sma.local; http://localhost; " +
                                                                                          "img-src 'self' data: http://sma.local; http://localhost; " +
                                                                                          "media-src 'self' data: http://sma.local; http://localhost; " +
                                                                                          "font-src 'self' http://sma.local; http://localhost; " +
                                                                                          "connect-src 'self' http://sma.local; http://localhost; " +
                                                                                          "frame-src 'self' http://sma.local; http://localhost; " +
                                                                                          "object-src 'self' http://sma.local; http://localhost; " +
                                                                                          "child-src 'self' http://sma.local; http://localhost; " +
                                                                                          "form-action 'self' http://sma.local; http://localhost; ")
                                                       )
                        );
        return http.build();
    }
}
