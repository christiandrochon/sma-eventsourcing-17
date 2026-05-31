package fr.cdrochon.smamonolithe.dossier.query.services;

import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.DOSSIER_ID;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

/**
 * Active l'extension Mockito de JUnit 5 pour initialiser et injecter automatiquement
 * les mocks utilises par ce test.
 */
@ExtendWith(MockitoExtension.class)
/**
 * Cette classe contient uniquement des tests unitaires.
 */
class DossierEventSourcingServiceImplTest {

    @Mock
    private EventStore eventStore;

    @Test
    void shouldReadEventsFromEventStore() {
        DomainEventStream stream = org.mockito.Mockito.mock(DomainEventStream.class);
        when(eventStore.readEvents(DOSSIER_ID)).thenReturn(stream);
        DossierEventSourcingServiceImpl service = new DossierEventSourcingServiceImpl(eventStore);

        DomainEventStream result = service.eventsByDossierId(DOSSIER_ID);

        assertSame(stream, result);
    }
}

