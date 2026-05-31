package fr.cdrochon.smamonolithe.dossier.query.controllers;

import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierListResponse;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetAllDossiersDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetDossierDTO;
import fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Active l'extension Mockito de JUnit 5 pour initialiser et injecter automatiquement
 * les mocks utilises par ce test.
 */
@ExtendWith(MockitoExtension.class)
class DossierQueryControllerTest {

    @Mock
    private QueryGateway queryGateway;

    @Mock
    private DossierRepository dossierRepository;

    @Mock
    private SubscriptionQueryResult<DossierQueryDTO, DossierQueryDTO> subscriptionQueryResult;

    @Test
    void shouldReturnDossierByIdAsync() {
        DossierQueryController controller = new DossierQueryController(queryGateway, dossierRepository);
        DossierQueryDTO expected = fr.cdrochon.smamonolithe.dossier.query.mapper.DossierQueryMapper.convertDossierToDossierDTO(sampleDossierEntity());
        when(queryGateway.query(any(GetDossierDTO.class), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(expected));

        DossierQueryDTO dto = controller.getDossierByIdAsync(DOSSIER_ID, adminAuth()).block();

        assertNotNull(dto);
        assertEquals(DOSSIER_ID, dto.getId());
        assertEquals("DOSSIER-001", dto.getNomDossier());
    }

    @Test
    void shouldReturnNullWhenDossierDoesNotExist() {
        DossierQueryController controller = new DossierQueryController(queryGateway, dossierRepository);
        when(queryGateway.query(any(GetDossierDTO.class), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        DossierQueryDTO dto = controller.getDossierByIdAsync(DOSSIER_ID, adminAuth()).block();

        assertNull(dto);
    }

    @Test
    void shouldReturnAllDossiersAsFlux() {
        DossierQueryController controller = new DossierQueryController(queryGateway, dossierRepository);
        DossierQueryDTO one = fr.cdrochon.smamonolithe.dossier.query.mapper.DossierQueryMapper.convertDossierToDossierDTO(sampleDossierEntity());
        DossierQueryDTO two = fr.cdrochon.smamonolithe.dossier.query.mapper.DossierQueryMapper.convertDossierToDossierDTO(sampleDossierEntity());
        two.setId("dos-2");
        when(queryGateway.query(any(GetAllDossiersDTO.class), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(new DossierListResponse(List.of(one, two))));

        StepVerifier.create(controller.getDossiersAsync(adminAuth()))
                .expectNextCount(2)
                .verifyComplete();
    }

    private Authentication adminAuth() {
        Map<String, Object> claims = Map.of(
                "realm_access", Map.of("roles", List.of("ADMIN")),
                "email", "admin@mail"
        );
        Jwt jwt = new Jwt("test-admin", Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "none"), claims);
        return new JwtAuthenticationToken(jwt);
    }

    @Test
    void shouldReturnInitialAndUpdatesForWatch() {
        DossierQueryController controller = new DossierQueryController(queryGateway, dossierRepository);
        DossierQueryDTO first = fr.cdrochon.smamonolithe.dossier.query.mapper.DossierQueryMapper.convertDossierToDossierDTO(sampleDossierEntity());
        DossierQueryDTO second = fr.cdrochon.smamonolithe.dossier.query.mapper.DossierQueryMapper.convertDossierToDossierDTO(sampleDossierEntity());
        second.setNomDossier("DOSSIER-UPDATE");

        when(queryGateway.subscriptionQuery(any(), any(ResponseType.class), any(ResponseType.class)))
                .thenReturn(subscriptionQueryResult);
        when(subscriptionQueryResult.initialResult()).thenReturn(Mono.just(first));
        when(subscriptionQueryResult.updates()).thenReturn(Flux.just(second));

        StepVerifier.create(controller.watch(DOSSIER_ID))
                .expectNext(first)
                .expectNext(second)
                .verifyComplete();
    }
}

