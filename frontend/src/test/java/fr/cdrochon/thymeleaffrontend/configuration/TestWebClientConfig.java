package fr.cdrochon.thymeleaffrontend.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.mockito.Mockito;
import fr.cdrochon.thymeleaffrontend.security.FrontendTokenResolver;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@TestConfiguration
@Import(TestSecurityConfig.class)
public class TestWebClientConfig {

    @Value("${external.service.url}")
    private String externalServiceUrl;

    @Bean
    public WebClient webClient() {
        // Assurer que le WebClient de test utilise la propriété dynamique external.service.url (le serveur factice)
        return WebClient.builder().baseUrl(externalServiceUrl).build();
    }

    // Fournir des beans mockés simples pour les dépendances courantes des tests afin
    // d'éviter l'utilisation de l'annotation @MockBean (parfois marquée dépréciée).
    // Les tests importent déjà cette configuration et obtiendront ces mocks dans
    // l'ApplicationContext.
    @Bean
    public FrontendTokenResolver frontendTokenResolver() {
        return Mockito.mock(FrontendTokenResolver.class);
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService() {
        return Mockito.mock(OAuth2AuthorizedClientService.class);
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager() {
        return Mockito.mock(OAuth2AuthorizedClientManager.class);
    }

    // NOTE: Le ClientRegistrationRepository est désormais fourni par
    // TestSecurityConfig pour séparer les responsabilités de sécurité des
    // autres mocks de test. Voir TestSecurityConfig.java.
}