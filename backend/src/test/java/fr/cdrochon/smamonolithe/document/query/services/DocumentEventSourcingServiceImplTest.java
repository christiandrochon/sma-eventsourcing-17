package fr.cdrochon.smamonolithe.document.query.services;

import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
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
class DocumentEventSourcingServiceImplTest {

    @Mock
    private EventStore eventStore;

    @Mock
    private DomainEventStream stream;

    private DocumentEventSourcingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DocumentEventSourcingServiceImpl(eventStore);
    }

    @Test
    void shouldDelegateReadEventsToEventStore() {
        when(eventStore.readEvents("doc-1")).thenReturn(stream);

        DomainEventStream result = service.eventsByDocumentId("doc-1");

        assertSame(stream, result);
        verify(eventStore).readEvents("doc-1");
    }

    @Test
    void shouldSupportAnyIdentifier() {
        when(eventStore.readEvents("another-id")).thenReturn(stream);

        DomainEventStream result = service.eventsByDocumentId("another-id");

        assertSame(stream, result);
    }
}
