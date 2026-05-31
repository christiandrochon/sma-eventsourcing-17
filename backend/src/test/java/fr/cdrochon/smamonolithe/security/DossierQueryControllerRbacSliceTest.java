package fr.cdrochon.smamonolithe.security;

import fr.cdrochon.smamonolithe.audit.infrastructure.AuditService;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.controllers.DossierQueryController;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierListResponse;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetAllDossiersDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetDossierDTO;
import fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.queryhandling.QueryGateway;
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
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

/**
 * Test slice WebFlux sur les endpoints dossiers pour verifier la RBAC HTTP
 * sans demarrer toute l'application.
 */
@WebFluxTest(controllers = DossierQueryController.class, properties = {
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
@Import({SecurityConfig.class, KeycloakReactiveJwtAuthenticationConverter.class, DossierQueryControllerRbacSliceTest.TestBeansConfig.class})
class DossierQueryControllerRbacSliceTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private QueryGateway queryGateway;

    @TestConfiguration
    static class TestBeansConfig {
        @Bean
        QueryGateway queryGateway() {
            return mock(QueryGateway.class);
        }

        @Bean
        DossierRepository dossierRepository() {
            DossierRepository repository = mock(DossierRepository.class);
            when(repository.findById(any())).thenReturn(Optional.empty());
            return repository;
        }

        @Bean
        AuditService auditService() {
            return mock(AuditService.class);
        }
    }

    @Test
    void shouldReturn401WhenNoJwtOnDossiersEndpoint() {
        webTestClient.get()
                .uri("/queries/dossiers")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowAdminToReadDossierById() {
        when(queryGateway.query(any(GetDossierDTO.class), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(dossier("dos-2", "other@mail")));

        webTestClient.mutateWith(mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        .jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("ADMIN")))
                                .claim("email", "admin@mail")))
                .get()
                .uri("/queries/dossiers/dos-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("dos-2");
    }

    @Test
    void shouldForbidUserWhenReadingOtherDossierById() {
        when(queryGateway.query(any(GetDossierDTO.class), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(dossier("dos-2", "other@mail")));

        webTestClient.mutateWith(mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        .jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER")))
                                .claim("email", "user@mail")))
                .get()
                .uri("/queries/dossiers/dos-2")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldFilterDossierListForUser() {
        when(queryGateway.query(any(GetAllDossiersDTO.class), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(new DossierListResponse(List.of(
                        dossier("dos-1", "user@mail"),
                        dossier("dos-2", "other@mail"),
                        dossier("dos-3", "user@mail")
                ))));

        webTestClient.mutateWith(mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        .jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER")))
                                .claim("email", "user@mail")))
                .get()
                .uri("/queries/dossiers")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("dos-1")
                .jsonPath("$[1].id").isEqualTo("dos-3")
                .jsonPath("$[2]").doesNotExist();
    }

    private static DossierQueryDTO dossier(String id, String email) {
        ClientQueryDTO client = new ClientQueryDTO();
        client.setId("cli-" + id);
        client.setMailClient(email);

        DossierQueryDTO dto = new DossierQueryDTO();
        dto.setId(id);
        dto.setClient(client);
        return dto;
    }
}

