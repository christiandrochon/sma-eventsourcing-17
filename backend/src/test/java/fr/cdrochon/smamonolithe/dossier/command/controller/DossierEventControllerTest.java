package fr.cdrochon.smamonolithe.dossier.command.controller;

import fr.cdrochon.smamonolithe.dossier.query.services.DossierEventSourcingService;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.DOSSIER_ID;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DossierEventControllerTest {

    @Mock
    private DossierEventSourcingService eventSourcingService;

    @Test
    void shouldDelegateEventLookupToService() {
        DomainEventStream stream = org.mockito.Mockito.mock(DomainEventStream.class);
        when(eventSourcingService.eventsByDossierId(DOSSIER_ID)).thenReturn(stream);
        DossierEventController controller = new DossierEventController(eventSourcingService);

        DomainEventStream result = controller.eventsById(DOSSIER_ID);

        assertSame(stream, result);
    }
}

