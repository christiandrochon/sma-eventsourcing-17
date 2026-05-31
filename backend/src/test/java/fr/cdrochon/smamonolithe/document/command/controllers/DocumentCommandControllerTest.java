package fr.cdrochon.smamonolithe.document.command.controllers;

import fr.cdrochon.smamonolithe.document.DocumentTestDataFactory;
import fr.cdrochon.smamonolithe.document.command.dtos.DocumentCommandDTO;
import fr.cdrochon.smamonolithe.document.command.services.DocumentCommandService;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.test.StepVerifier;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Active l'extension Mockito de JUnit 5 pour initialiser et injecter automatiquement
 * les mocks utilises par ce test.
 */
@ExtendWith(MockitoExtension.class)
class DocumentCommandControllerTest {

    @Mock
    private DocumentCommandService commandService;

    @Mock
    private EventStore eventStore;

    @Mock
    private DomainEventStream domainEventStream;

    private DocumentCommandController controller;

    @BeforeEach
    void setUp() {
        controller = new DocumentCommandController(commandService, eventStore);
    }

    @Test
    void shouldReturnCreatedResponseWhenCreateSucceeds() {
        DocumentCommandDTO dto = DocumentTestDataFactory.sampleCommandDTO();
        when(commandService.createDocument(any())).thenReturn(CompletableFuture.completedFuture(dto));

        StepVerifier.create(controller.createClientAsync(dto))
                .assertNext(response -> {
                    assertEquals(HttpStatus.CREATED, response.getStatusCode());
                    assertEquals(dto, response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnInternalServerErrorWhenCreateFails() {
        DocumentCommandDTO dto = DocumentTestDataFactory.sampleCommandDTO();
        CompletableFuture<DocumentCommandDTO> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("failure"));
        when(commandService.createDocument(any())).thenReturn(failed);

        StepVerifier.create(controller.createClientAsync(dto))
                .assertNext(response -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()))
                .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReadEventsFromEventStoreById() {
        DomainEventMessage<?> event1 = mock(DomainEventMessage.class);
        DomainEventMessage<?> event2 = mock(DomainEventMessage.class);
        when(eventStore.readEvents("doc-1")).thenReturn(domainEventStream);
        when(domainEventStream.asStream()).thenReturn((Stream) Stream.of(event1, event2));

        Stream<?> stream = controller.readDocumentsInEventStore("doc-1");

        assertEquals(2, stream.count());
    }

    @Test
    void shouldReturn500MessageFromExceptionHandler() {
        var response = controller.exceptionHandler(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("boom"));
    }
}
