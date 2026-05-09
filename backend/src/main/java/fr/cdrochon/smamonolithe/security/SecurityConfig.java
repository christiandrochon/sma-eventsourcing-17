package fr.cdrochon.smamonolithe.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableReactiveMethodSecurity
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "true")
public class SecurityConfig {

	@Value("${app.security.require-authenticated-all:false}")
	private boolean requireAuthenticatedAll;

	@Value("${app.security.audit-endpoints-authenticated:true}")
	private boolean auditEndpointsAuthenticated;

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		ServerHttpSecurity.AuthorizeExchangeSpec authorize = http
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeExchange();

		authorize
				.pathMatchers("/actuator/health", "/actuator/info").permitAll()
				.pathMatchers("/v3/**", "/swagger-ui/**").permitAll()
				.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll();

		if (auditEndpointsAuthenticated) {
			authorize.pathMatchers("/audit/compliance/**").authenticated();
		} else {
			authorize.pathMatchers("/audit/compliance/**").permitAll();
		}

		if (requireAuthenticatedAll) {
			authorize.anyExchange().authenticated();
		} else {
			authorize.anyExchange().permitAll();
		}

		return http
				.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {}))
				.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(List.of("*"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setExposedHeaders(List.of("Authorization"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
