package fr.cdrochon.smamonolithe.document.command.controllers;

import fr.cdrochon.smamonolithe.document.query.services.DocumentEventSourcingService;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentEventControllerTest {

    @Mock
    private DocumentEventSourcingService eventSourcingService;

    @Mock
    private DomainEventStream stream;

    private DocumentEventController controller;

    @BeforeEach
    void setUp() {
        controller = new DocumentEventController(eventSourcingService);
    }

    @Test
    void shouldDelegateEventsByIdToService() {
        when(eventSourcingService.eventsByDocumentId("doc-1")).thenReturn(stream);

        DomainEventStream result = controller.eventsById("doc-1");

        assertSame(stream, result);
        verify(eventSourcingService).eventsByDocumentId("doc-1");
    }

    @Test
    void shouldSupportAnyIdentifierValue() {
        when(eventSourcingService.eventsByDocumentId("any-id")).thenReturn(stream);

        DomainEventStream result = controller.eventsById("any-id");

        assertSame(stream, result);
    }
}
