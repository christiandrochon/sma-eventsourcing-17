package fr.cdrochon.thymeleaffrontend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import fr.cdrochon.thymeleaffrontend.configuration.TestWebClientConfig;
import fr.cdrochon.thymeleaffrontend.security.FrontendTokenResolver;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestWebClientConfig.class)
@org.springframework.test.context.TestPropertySource(properties = {
        "app.security.enabled=false"
})
class ThymeleafFrontendApplicationTests {

    // Remplacement de l'utilisation de @MockBean : on s'appuie sur TestWebClientConfig
    // qui fournit un bean mocké FrontendTokenResolver. Cela évite les avertissements
    // de dépréciation dans l'IDE pour @MockBean et centralise la configuration des mocks.
    @org.springframework.beans.factory.annotation.Autowired
    @SuppressWarnings("unused")
    private FrontendTokenResolver frontendTokenResolver;

    @Test
    void contextLoads() {
    }
    
}
