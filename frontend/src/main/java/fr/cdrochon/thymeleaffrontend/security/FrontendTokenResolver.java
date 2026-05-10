package fr.cdrochon.thymeleaffrontend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class FrontendTokenResolver {

	private final OAuth2AuthorizedClientService authorizedClientService;

	public FrontendTokenResolver(OAuth2AuthorizedClientService authorizedClientService) {
		this.authorizedClientService = authorizedClientService;
	}

	public String resolveAccessToken(Authentication authentication) {
		if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
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

