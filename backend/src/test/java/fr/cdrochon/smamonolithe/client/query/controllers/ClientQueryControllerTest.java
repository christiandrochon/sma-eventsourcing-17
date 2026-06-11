package fr.cdrochon.smamonolithe.client.query.controllers;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientListResponse;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetAllClientsDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetClientDTO;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Active l'extension Mockito de JUnit 5 pour initialiser et injecter automatiquement
 * les mocks utilises par ce test.
 */
@ExtendWith(MockitoExtension.class)
/**
 * Cette classe contient uniquement des tests unitaires.
 */
class ClientQueryControllerTest {

    @Mock
    private QueryGateway queryGateway;

    @Test
    void shouldReturnClientByIdForAdminEvenIfOwnedByAnotherUser() {
        ClientQueryController controller = new ClientQueryController(queryGateway);
        when(queryGateway.query(any(GetClientDTO.class), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(client("cli-2", "other@mail")));

        StepVerifier.create(controller.getClientByIdAsync("cli-2", auth("admin@mail", "ADMIN")))
                .assertNext(dto -> {
                    assertEquals("cli-2", dto.getId());
                    assertEquals("other@mail", dto.getMailClient());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnClientByIdForUserWhenOwned() {
        ClientQueryController controller = new ClientQueryController(queryGateway);
        when(queryGateway.query(any(GetClientDTO.class), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(client("cli-1", "user@mail")));

        StepVerifier.create(controller.getClientByIdAsync("cli-1", auth("user@mail", "USER")))
                .assertNext(dto -> {
                    assertEquals("cli-1", dto.getId());
                    assertEquals("user@mail", dto.getMailClient());
                })
                .verifyComplete();
    }

    @Test
    void shouldForbidClientByIdForUserWhenNotOwned() {
        ClientQueryController controller = new ClientQueryController(queryGateway);
        when(queryGateway.query(any(GetClientDTO.class), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(client("cli-2", "other@mail")));

        StepVerifier.create(controller.getClientByIdAsync("cli-2", auth("user@mail", "USER")))
                .expectErrorSatisfies(error -> {
                    assertEquals(403, ((ResponseStatusException) error).getStatusCode().value());
                })
                .verify();
    }

    @Test
    void shouldReturnAllClientsForAdmin() {
        ClientQueryController controller = new ClientQueryController(queryGateway);
        when(queryGateway.query(any(GetAllClientsDTO.class), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(new ClientListResponse(List.of(
                        client("cli-1", "user@mail"),
                        client("cli-2", "other@mail")
                ))));

        StepVerifier.create(controller.getClientsAsync(auth("admin@mail", "ADMIN")).collectList())
                .assertNext(list -> {
                    assertEquals(2, list.size());
                    assertEquals("cli-1", list.get(0).getId());
                    assertEquals("cli-2", list.get(1).getId());
                })
                .verifyComplete();

        verify(queryGateway).query(any(GetAllClientsDTO.class), any(ResponseType.class));
    }

    @Test
    void shouldReturnOnlyOwnClientsForUser() {
        ClientQueryController controller = new ClientQueryController(queryGateway);
        when(queryGateway.query(any(GetAllClientsDTO.class), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(new ClientListResponse(List.of(
                        client("cli-1", "user@mail"),
                        client("cli-2", "other@mail"),
                        client("cli-3", "user@mail")
                ))));

        StepVerifier.create(controller.getClientsAsync(auth("user@mail", "USER")).collectList())
                .assertNext(list -> {
                    assertEquals(2, list.size());
                    assertEquals(List.of("cli-1", "cli-3"), list.stream().map(ClientQueryDTO::getId).toList());
                })
                .verifyComplete();
    }

    @Test
    void shouldForbidClientListForUserWithoutEmail() {
        ClientQueryController controller = new ClientQueryController(queryGateway);
        when(queryGateway.query(any(GetAllClientsDTO.class), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(new ClientListResponse(List.of(client("cli-1", "user@mail")))));

        StepVerifier.create(controller.getClientsAsync(authWithoutEmail("USER")))
                .expectErrorSatisfies(error -> {
                    assertEquals(403, ((ResponseStatusException) error).getStatusCode().value());
                })
                .verify();
    }

    private static Authentication auth(String email, String role) {
        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("realm_access", Map.of("roles", List.of(role)));
        claims.put("email", email);
        Jwt jwt = new Jwt("token-" + role + "-" + email,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                claims);
        return new JwtAuthenticationToken(jwt, List.of(), email);
    }

    private static Authentication authWithoutEmail(String role) {
        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("realm_access", Map.of("roles", List.of(role)));
        Jwt jwt = new Jwt("token-" + role,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                claims);
        return new JwtAuthenticationToken(jwt, List.of(), "anonymous");
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

