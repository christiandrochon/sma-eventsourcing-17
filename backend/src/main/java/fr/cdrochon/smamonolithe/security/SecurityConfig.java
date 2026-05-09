package fr.cdrochon.smamonolithe.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.util.StringUtils;
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

	@Value("${app.security.audit-required-roles:ADMIN,AUDITOR}")
	private String auditRequiredRoles;

	@Value("${app.security.audit-writer-roles:ADMIN}")
	private String auditWriterRoles;

	private final KeycloakReactiveJwtAuthenticationConverter jwtAuthenticationConverter;

	public SecurityConfig(KeycloakReactiveJwtAuthenticationConverter jwtAuthenticationConverter) {
		this.jwtAuthenticationConverter = jwtAuthenticationConverter;
	}

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
			authorize
					.pathMatchers(HttpMethod.GET, "/audit/compliance/**").hasAnyRole(resolveAuditRoles())
					.pathMatchers(HttpMethod.POST, "/audit/compliance/**").hasAnyRole(resolveAuditWriterRoles())
					.pathMatchers("/audit/compliance/**").denyAll();
		} else {
			authorize.pathMatchers("/audit/compliance/**").permitAll();
		}

		// --- Règles métier ---
		// ADMIN uniquement : création de client et de dossier, liste de tous les clients
		authorize
				.pathMatchers(HttpMethod.POST, "/commands/createClient").hasRole("ADMIN")
				.pathMatchers(HttpMethod.POST, "/commands/createDossier").hasRole("ADMIN")
				.pathMatchers(HttpMethod.GET, "/queries/clients").hasRole("ADMIN")
				// ADMIN ou USER : création de véhicule et de document
				.pathMatchers(HttpMethod.POST, "/commands/createVehicule").hasAnyRole("ADMIN", "USER")
				.pathMatchers(HttpMethod.POST, "/commands/createDocument").hasAnyRole("ADMIN", "USER")
				// ADMIN ou USER : consultation d'un dossier, d'un client (son propre compte - contrôle métier à la couche service)
				.pathMatchers(HttpMethod.GET, "/queries/dossiers/**").hasAnyRole("ADMIN", "USER")
				.pathMatchers(HttpMethod.GET, "/queries/dossier/**").hasAnyRole("ADMIN", "USER")
				.pathMatchers(HttpMethod.GET, "/queries/clients/{id}").hasAnyRole("ADMIN", "USER")
				.pathMatchers(HttpMethod.GET, "/queries/client/**").hasAnyRole("ADMIN", "USER")
				// ADMIN ou USER : consultation des documents et véhicules
				.pathMatchers(HttpMethod.GET, "/queries/documents/**").hasAnyRole("ADMIN", "USER")
				.pathMatchers(HttpMethod.GET, "/queries/vehicules/**").hasAnyRole("ADMIN", "USER");

		if (requireAuthenticatedAll) {
			authorize.anyExchange().authenticated();
		} else {
			authorize.anyExchange().permitAll();
		}

		return http
				.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
				.build();
	}

	private String[] resolveAuditRoles() {
		String[] roles = StringUtils.commaDelimitedListToStringArray(auditRequiredRoles);
		return normalizeRoles(roles, new String[]{"ADMIN", "AUDITOR"});
	}

	private String[] resolveAuditWriterRoles() {
		String[] roles = StringUtils.commaDelimitedListToStringArray(auditWriterRoles);
		return normalizeRoles(roles, new String[]{"ADMIN"});
	}

	private String[] normalizeRoles(String[] roles, String[] fallback) {
		if (roles.length == 0) {
			return fallback;
		}
		for (int i = 0; i < roles.length; i++) {
			roles[i] = roles[i].trim().replace("ROLE_", "");
		}
		return roles;
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
