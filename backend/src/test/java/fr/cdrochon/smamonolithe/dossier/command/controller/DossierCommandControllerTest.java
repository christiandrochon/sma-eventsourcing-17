package fr.cdrochon.smamonolithe.dossier.command.controller;

import fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandDTO;
import fr.cdrochon.smamonolithe.dossier.command.services.DossierCommandService;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.util.stream.Stream;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Active l'extension Mockito de JUnit 5 pour initialiser et injecter automatiquement
 * les mocks utilises par ce test.
 */
@ExtendWith(MockitoExtension.class)
class DossierCommandControllerTest {

    @Mock
    private EventStore eventStore;

    @Mock
    private DossierCommandService dossierCommandService;

    @Test
    void shouldReturnCreatedWhenCreateSucceeds() {
        DossierCommandController controller = new DossierCommandController(eventStore, dossierCommandService);
        DossierCommandDTO dto = sampleDossierCommandDto();
        when(dossierCommandService.createDossier(any(DossierCommandDTO.class)))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(dto));

        ResponseEntity<DossierCommandDTO> response = controller.createClientAsync(dto).block(Duration.ofSeconds(2));

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void shouldReturnInternalServerErrorWhenCreateFails() {
        DossierCommandController controller = new DossierCommandController(eventStore, dossierCommandService);
        DossierCommandDTO dto = sampleDossierCommandDto();
        when(dossierCommandService.createDossier(any(DossierCommandDTO.class)))
                .thenReturn(java.util.concurrent.CompletableFuture.failedFuture(new RuntimeException("ko")));

        ResponseEntity<DossierCommandDTO> response = controller.createClientAsync(dto).block(Duration.ofSeconds(2));

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldReadEventsFromEventStore() {
        DossierCommandController controller = new DossierCommandController(eventStore, dossierCommandService);
        DomainEventStream domainEventStream = org.mockito.Mockito.mock(DomainEventStream.class);
        DomainEventMessage<?> message = org.mockito.Mockito.mock(DomainEventMessage.class);
        Stream expected = Stream.of(message);
        when(eventStore.readEvents(DOSSIER_ID)).thenReturn(domainEventStream);
        when(domainEventStream.asStream()).thenReturn(expected);

        Stream<?> result = controller.readDossiersInEventStore(DOSSIER_ID);

        assertSame(expected, result);
    }

    @Test
    void shouldBuildExceptionResponse() {
        DossierCommandController controller = new DossierCommandController(eventStore, dossierCommandService);

        ResponseEntity<String> response = controller.exceptionHandler(new IllegalStateException("err"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("err", response.getBody());
    }
}

