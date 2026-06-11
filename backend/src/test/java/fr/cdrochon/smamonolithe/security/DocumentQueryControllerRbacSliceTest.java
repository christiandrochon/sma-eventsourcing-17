package fr.cdrochon.smamonolithe.security;

import fr.cdrochon.smamonolithe.audit.infrastructure.AuditService;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.document.query.controllers.DocumentQueryController;
import fr.cdrochon.smamonolithe.document.query.entities.Document;
import fr.cdrochon.smamonolithe.document.query.repositories.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

/**
 * Test slice WebFlux sur les endpoints documents pour verifier les statuts
 * HTTP et la logique RBAC sans contexte applicatif complet.
 */
@WebFluxTest(controllers = DocumentQueryController.class, properties = {
        "app.security.enabled=true",
        "app.security.require-authenticated-all=false",
        "app.security.audit-endpoints-authenticated=true",
        "app.security.audit-required-roles=ADMIN,AUDITOR",
        "app.security.audit-writer-roles=ADMIN",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:18080/realms/sma-realm/protocol/openid-connect/certs"
})
/**
 * @Import complete le slice @WebFluxTest avec la securite reactive JWT
 * et les beans de test necessaires au contexte minimal.
 */
@Import({SecurityConfig.class, KeycloakReactiveJwtAuthenticationConverter.class, DocumentQueryControllerRbacSliceTest.TestBeansConfig.class})
class DocumentQueryControllerRbacSliceTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DocumentRepository documentRepository;

    @TestConfiguration
    static class TestBeansConfig {
        @Bean
        DocumentRepository documentRepository() {
            return mock(DocumentRepository.class);
        }

        @Bean
        AuditService auditService() {
            return mock(AuditService.class);
        }
    }

    @Test
    void shouldReturn401WhenNoJwtOnDocumentsEndpoint() {
        webTestClient.get()
                .uri("/queries/documents")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowAdminToReadDocumentById() {
        when(documentRepository.findById("doc-2")).thenReturn(Optional.of(document("doc-2", "other@mail")));

        webTestClient.mutateWith(mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        .jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("ADMIN")))
                                .claim("email", "admin@mail")))
                .get()
                .uri("/queries/documents/doc-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("doc-2");
    }

    @Test
    void shouldForbidUserWhenReadingOtherDocumentById() {
        when(documentRepository.findById("doc-2")).thenReturn(Optional.of(document("doc-2", "other@mail")));

        webTestClient.mutateWith(mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        .jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER")))
                                .claim("email", "user@mail")))
                .get()
                .uri("/queries/documents/doc-2")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturnUserDocumentListFromOwnerQuery() {
        when(documentRepository.findByClientMailClient("user@mail"))
                .thenReturn(List.of(document("doc-1", "user@mail"), document("doc-3", "user@mail")));
        when(documentRepository.findAll()).thenReturn(List.of(document("doc-admin", "admin@mail")));

        webTestClient.mutateWith(mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        .jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER")))
                                .claim("email", "user@mail")))
                .get()
                .uri("/queries/documents")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("doc-1")
                .jsonPath("$[1].id").isEqualTo("doc-3")
                .jsonPath("$[2]").doesNotExist();
    }

    private static Document document(String id, String email) {
        Client client = new Client();
        client.setId("cli-" + id);
        client.setMailClient(email);

        Document document = new Document();
        document.setId(id);
        document.setNomDocument("nom-" + id);
        document.setClient(client);
        return document;
    }
}

