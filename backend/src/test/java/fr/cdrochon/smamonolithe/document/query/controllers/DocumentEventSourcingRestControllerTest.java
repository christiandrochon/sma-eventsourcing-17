package fr.cdrochon.smamonolithe.document.query.controllers;

import fr.cdrochon.smamonolithe.document.query.services.DocumentEventSourcingService;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Active l'extension Mockito de JUnit 5 pour initialiser et injecter automatiquement
 * les mocks utilises par ce test.
 */
@ExtendWith(MockitoExtension.class)
class DocumentEventSourcingRestControllerTest {

    @Mock
    private DocumentEventSourcingService eventSourcingService;

    @Mock
    private DomainEventStream stream;

    private DocumentEventSourcingRestController controller;

    @BeforeEach
    void setUp() {
        controller = new DocumentEventSourcingRestController(eventSourcingService);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnEventStreamFromService() {
        DomainEventMessage<?> event1 = mock(DomainEventMessage.class);
        DomainEventMessage<?> event2 = mock(DomainEventMessage.class);
        DomainEventMessage<?> event3 = mock(DomainEventMessage.class);

        when(eventSourcingService.eventsByDocumentId("doc-1")).thenReturn(stream);
        when(stream.asStream()).thenReturn((Stream) Stream.of(event1, event2, event3));

        Stream<?> result = controller.eventsByDocumentId("doc-1");

        assertEquals(3, result.count());
        verify(eventSourcingService).eventsByDocumentId("doc-1");
    }

    @Test
    void shouldHandleEmptyEventStream() {
        when(eventSourcingService.eventsByDocumentId("empty")).thenReturn(stream);
        when(stream.asStream()).thenReturn(Stream.empty());

        Stream<?> result = controller.eventsByDocumentId("empty");

        assertEquals(0, result.count());
    }
}
