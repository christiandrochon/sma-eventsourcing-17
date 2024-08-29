package fr.cdrochon.smamonolithe.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration

public class CSPConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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
                        )
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                                               .anyRequest().permitAll()
                                      )
                .csrf(csrf -> csrf.disable())
//                .formLogin(formLogin -> formLogin.disable())
//                .httpBasic(httpBasic -> httpBasic.disable())
        
        //                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
        //                                               .anyRequest().authenticated()
        //                                      )
        //                .formLogin(formLogin -> formLogin
        //                                   .permitAll()
        //                          )
        //                .logout(logout -> logout
        //                        .permitAll()
        //                       )
        ;
        return http.build();
    }
    
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://sma.local"); // Replace with your client's origin
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
