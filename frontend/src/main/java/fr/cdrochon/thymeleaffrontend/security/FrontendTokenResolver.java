package fr.cdrochon.thymeleaffrontend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class FrontendTokenResolver {

	// Injection optionnelle : il est possible que le contexte n'expose pas
	// d'OAuth2AuthorizedClientService (tests/contexts légers). Nous utilisons
	// ObjectProvider pour récupérer le bean si disponible sans provoquer
	// d'erreur d'injection lorsque le bean est absent.
	private OAuth2AuthorizedClientService authorizedClientService;

	public FrontendTokenResolver(ObjectProvider<OAuth2AuthorizedClientService> authorizedClientServiceProvider) {
		this.authorizedClientService = authorizedClientServiceProvider.getIfAvailable();
	}

	public String resolveAccessToken(Authentication authentication) {
		if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
			return null;
		}
		if (authorizedClientService == null) {
			// Pas de service disponible (p.ex. configuration minimale pour des tests) :
			// on ne peut pas résoudre de token.
			return null;
		}

		OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
				oauthToken.getAuthorizedClientRegistrationId(),
				oauthToken.getName()
		);
		if (client == null || client.getAccessToken() == null) {
			return null;
		}
		return client.getAccessToken().getTokenValue();
	}
}

