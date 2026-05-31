package fr.cdrochon.smamonolithe.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cette classe contient uniquement des tests unitaires.
 */
class ClientWebConfigTest {

    @Test
    void shouldCreateRestTemplateWithTechnicalInterceptor() {
        ClientWebConfig config = new ClientWebConfig();
        ReflectionTestUtils.setField(config, "externalServiceUrl", "http://localhost:8091");

        RestTemplate restTemplate = config.restTemplate();

        assertNotNull(restTemplate);
        assertFalse(restTemplate.getInterceptors().isEmpty());
    }

    @Test
    void shouldCreateWebClientWithBaseUrl() {
        ClientWebConfig config = new ClientWebConfig();
        ReflectionTestUtils.setField(config, "externalServiceUrl", "http://localhost:8091");

        WebClient webClient = config.webClient();

        assertNotNull(webClient);
    }
}

