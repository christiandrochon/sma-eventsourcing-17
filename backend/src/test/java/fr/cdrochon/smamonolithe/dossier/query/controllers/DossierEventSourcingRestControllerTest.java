package fr.cdrochon.smamonolithe.dossier.query.controllers;

import fr.cdrochon.smamonolithe.dossier.query.services.DossierEventSourcingService;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.DOSSIER_ID;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DossierEventSourcingRestControllerTest {

    @Mock
    private DossierEventSourcingService service;

    @Test
    void shouldReturnEventStreamAsJavaStream() {
        DossierEventSourcingRestController controller = new DossierEventSourcingRestController(service);
        DomainEventStream eventStream = org.mockito.Mockito.mock(DomainEventStream.class);
        DomainEventMessage<?> message = org.mockito.Mockito.mock(DomainEventMessage.class);
        Stream asStream = Stream.of(message);
        when(service.eventsByDossierId(DOSSIER_ID)).thenReturn(eventStream);
        when(eventStream.asStream()).thenReturn(asStream);

        Stream<?> result = controller.eventsByAccountId(DOSSIER_ID);

        assertSame(asStream, result);
    }
}

