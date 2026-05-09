package fr.cdrochon.smamonolithe.dossier.query.controllers;

import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierQueryDTO;
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

import java.util.List;
import java.util.Optional;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
        when(dossierRepository.findById(DOSSIER_ID)).thenReturn(Optional.of(sampleDossierEntity()));

        DossierQueryDTO dto = controller.getDossierByIdAsync(DOSSIER_ID).block();

        assertNotNull(dto);
        assertEquals(DOSSIER_ID, dto.getId());
        assertEquals("DOSSIER-001", dto.getNomDossier());
    }

    @Test
    void shouldReturnNullWhenDossierDoesNotExist() {
        DossierQueryController controller = new DossierQueryController(queryGateway, dossierRepository);
        when(dossierRepository.findById(DOSSIER_ID)).thenReturn(Optional.empty());

        DossierQueryDTO dto = controller.getDossierByIdAsync(DOSSIER_ID).block();

        assertNull(dto);
    }

    @Test
    void shouldReturnAllDossiersAsFlux() {
        DossierQueryController controller = new DossierQueryController(queryGateway, dossierRepository);
        when(dossierRepository.findAll()).thenReturn(List.of(sampleDossierEntity(), sampleDossierEntity()));

        StepVerifier.create(controller.getDossiersAsync(null))
                .expectNextCount(2)
                .verifyComplete();
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

