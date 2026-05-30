package fr.cdrochon.thymeleaffrontend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import fr.cdrochon.thymeleaffrontend.configuration.TestWebClientConfig;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import fr.cdrochon.thymeleaffrontend.security.FrontendTokenResolver;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestWebClientConfig.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.security.enabled=true"
})
class FrontendPublicLandingSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // Utiliser le bean mocké fourni par TestWebClientConfig au lieu de @MockBean.
    @org.springframework.beans.factory.annotation.Autowired
    @SuppressWarnings("unused")
    private FrontendTokenResolver frontendTokenResolver;

    // Fournir un ClientRegistrationRepository en mémoire réel avec une inscription
    // 'keycloak' factice afin que l'infrastructure oauth2Login enregistre
    // l'endpoint '/oauth2/authorization/keycloak'.
    @Import(TestOAuth2ClientsConfig.class)
    static class ImportConfig {}

    // Mock fourni par TestWebClientConfig
    @org.springframework.beans.factory.annotation.Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    // Mock fourni par TestWebClientConfig
    @org.springframework.beans.factory.annotation.Autowired
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @TestConfiguration
    static class TestOAuth2ClientsConfig {
        @Bean
        public ClientRegistrationRepository clientRegistrationRepository() {
            ClientRegistration reg = ClientRegistration.withRegistrationId("keycloak")
                    .clientId("dummy-client")
                    .clientSecret("secret")
                    .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .authorizationUri("http://auth-server/auth")
                    .tokenUri("http://auth-server/token")
                    .userInfoUri("http://auth-server/userinfo")
                    .userNameAttributeName("sub")
                    .clientName("keycloak")
                    .build();
            return new org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository(reg);
        }
    }

    @Test
    void shouldKeepLandingPagePublicWhenSecurityEnabled() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Entrez dans l'appli")));
    }

    @Test
    void shouldKeepStaticAssetsPublicWhenSecurityEnabled() throws Exception {
        mockMvc.perform(get("/assets/dist/css/bootstrap.min.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));
    }

    @Test
    void shouldRedirectProtectedPageToOAuthLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/dossiers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/keycloak"));
    }
}
