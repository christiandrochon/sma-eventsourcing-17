//package fr.cdrochon.smamonolithe.garage.security;
//
//import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
//import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
//import org.springframework.security.core.session.SessionRegistryImpl;
//import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
//import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
//
////@KeycloakConfiguration
////@EnableWebSecurity
//public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {
//
////    @Bean
////    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
////        return new KeycloakSpringBootConfigResolver();
////    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        super.configure(http);
//        http
//                .authorizeRequests()
//                .antMatchers("/public/**").permitAll()
//                .antMatchers(HttpMethod.POST, "/commands/**").hasRole("USER")
//                .anyRequest().authenticated()
//                .and()
//                .oauth2Login()
//                .and()
//                .logout()
//                .logoutUrl("/logout")
//                .addLogoutHandler(keycloakLogoutHandler())
//                .logoutSuccessUrl("/");
//    }
//
//    @Bean
//    @Override
//    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
//        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
//    }
//
//    @Bean
////    @Override
//    protected SimpleAuthorityMapper grantedAuthoritiesMapper() {
//        SimpleAuthorityMapper authorityMapper = new SimpleAuthorityMapper();
//        authorityMapper.setConvertToUpperCase(true);
//        authorityMapper.setDefaultAuthority("USER");
//        return authorityMapper;
//    }
//}
