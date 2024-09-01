package fr.cdrochon.smamonolithe.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration

public class CspConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChainCsp(HttpSecurity http) throws Exception {
        http
                // desactive la CSP
                //                .headers(headers -> {})
                // active la CSP
                .headers(headers -> headers
                                 .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives("default-src 'self' http://sma.local; " +
                                                                                          "script-src 'self' 'unsafe-inline' http://sma.local; " +
                                                                                          "script-src-elem 'self' 'unsafe-inline' http://sma.local; " +
                                                                                          "script-src-attr 'self' 'unsafe-inline' http://sma.local; " +
                                                                                          "style-src 'self' 'unsafe-inline' http://sma.local; " +
                                                                                          "img-src 'self' data: http://sma.local; " +
                                                                                          "media-src 'self' data: http://sma.local; " +
                                                                                          "font-src 'self' http://sma.local; " +
                                                                                          "connect-src 'self' http://sma.local; " +
                                                                                          "frame-src 'self' http://sma.local; " +
                                                                                          "object-src 'self' http://sma.local; " +
                                                                                          "child-src 'self' http://sma.local; " +
                                                                                          "form-action 'self' http://sma.local;")
                                                       )
                        );
        return http.build();
    }
}
