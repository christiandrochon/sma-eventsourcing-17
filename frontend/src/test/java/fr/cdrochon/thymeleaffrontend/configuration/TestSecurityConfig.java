package fr.cdrochon.thymeleaffrontend.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.mockito.Mockito;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * Configuration de test dédiée aux beans liés à la sécurité OAuth2.
 *
 * Raison : séparer les mocks de sécurité (ClientRegistrationRepository, etc.)
 * des autres mocks (WebClient, token resolver) pour permettre aux tests qui
 * souhaitent fournir une configuration OAuth réelle (p.ex. InMemoryClientRegistrationRepository)
 * de l'overrider simplement via @Import d'une configuration de test locale.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @ConditionalOnMissingBean(ClientRegistrationRepository.class)
    @ConditionalOnProperty(name = "app.security.enabled", havingValue = "false", matchIfMissing = true)
    public ClientRegistrationRepository clientRegistrationRepository() {
        // Fournit un mock par défaut. Les tests qui ont besoin d'enregistrements
        // réels doivent déclarer leur propre ClientRegistrationRepository ;
        // la condition @ConditionalOnMissingBean permettra l'override.
        return Mockito.mock(ClientRegistrationRepository.class);
    }
}

