package fr.cdrochon.smamonolithe.security;

import fr.cdrochon.smamonolithe.audit.infrastructure.AuditService;
import fr.cdrochon.smamonolithe.client.query.controllers.ClientQueryController;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientListResponse;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetAllClientsDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetClientDTO;
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
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

/**
 * Slice WebFlux sur un controller critique: verification RBAC HTTP (401/403/200)
 * sans charger tout le contexte de l'application.
 */
@WebFluxTest(controllers = ClientQueryController.class, properties = {
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
@Import({SecurityConfig.class, KeycloakReactiveJwtAuthenticationConverter.class, ClientQueryControllerRbacSliceTest.TestBeansConfig.class})
class ClientQueryControllerRbacSliceTest {

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
        AuditService auditService() {
            return mock(AuditService.class);
        }
    }

    @Test
    void shouldReturn401WhenNoJwtOnClientsEndpoint() {
        webTestClient.get()
                .uri("/queries/clients")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowAdminToReadClientById() {
        when(queryGateway.query(any(GetClientDTO.class), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(client("cli-2", "other@mail")));

        webTestClient.mutateWith(mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        .jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("ADMIN")))
                                .claim("email", "admin@mail")))
                .get()
                .uri("/queries/clients/cli-2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("cli-2");
    }

    @Test
    void shouldForbidUserWhenReadingOtherClientById() {
        when(queryGateway.query(any(GetClientDTO.class), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(client("cli-2", "other@mail")));

        webTestClient.mutateWith(mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        .jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER")))
                                .claim("email", "user@mail")))
                .get()
                .uri("/queries/clients/cli-2")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldFilterClientsForUserByEmail() {
        when(queryGateway.query(any(GetAllClientsDTO.class), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(new ClientListResponse(List.of(
                        client("cli-1", "user@mail"),
                        client("cli-2", "other@mail"),
                        client("cli-3", "user@mail")
                ))));

        webTestClient.mutateWith(mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        .jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER")))
                                .claim("email", "user@mail")))
                .get()
                .uri("/queries/clients")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("cli-1")
                .jsonPath("$[1].id").isEqualTo("cli-3")
                .jsonPath("$[2]").doesNotExist();
    }

    private static ClientQueryDTO client(String id, String email) {
        ClientQueryDTO dto = new ClientQueryDTO();
        dto.setId(id);
        dto.setNomClient("Nom-" + id);
        dto.setPrenomClient("Prenom-" + id);
        dto.setMailClient(email);
        return dto;
    }
}

