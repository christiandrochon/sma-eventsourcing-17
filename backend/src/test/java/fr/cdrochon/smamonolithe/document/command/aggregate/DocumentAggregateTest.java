package fr.cdrochon.smamonolithe.document.command.aggregate;

import fr.cdrochon.smamonolithe.document.DocumentTestDataFactory;
import fr.cdrochon.smamonolithe.document.command.commands.DocumentCreateCommand;
import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import fr.cdrochon.smamonolithe.document.events.DocumentCreatedEvent;
import fr.cdrochon.smamonolithe.garage.command.exceptions.CreatedGarageException;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DocumentAggregateTest {

    private AggregateTestFixture<DocumentAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(DocumentAggregate.class);
    }

    @Test
    void shouldPublishDocumentCreatedEventOnValidCommand() {
        fixture.givenNoPriorActivity()
                .when(DocumentTestDataFactory.sampleCreateCommand())
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(org.axonframework.test.matchers.Matchers.sequenceOf(
                        org.axonframework.test.matchers.Matchers.messageWithPayload(instanceOf(DocumentCreatedEvent.class))
                ));
    }

    @Test
    void shouldThrowExceptionWhenNomDocumentIsNull() {
        DocumentCreateCommand command = new DocumentCreateCommand(
                "doc-1",
                null,
                "Titre",
                "Service",
                DocumentTestDataFactory.sampleTypeDocument(),
                DocumentTestDataFactory.creationInstant(),
                DocumentTestDataFactory.modificationInstant(),
                DocumentStatusDTO.CREATED
        );

        fixture.givenNoPriorActivity()
                .when(command)
                .expectException(CreatedGarageException.class);
    }

    @Test
    void shouldThrowMeaningfulExceptionMessageWhenNomDocumentIsNull() {
        DocumentCreateCommand command = new DocumentCreateCommand(
                "doc-1",
                null,
                "Titre",
                "Service",
                DocumentTestDataFactory.sampleTypeDocument(),
                DocumentTestDataFactory.creationInstant(),
                DocumentTestDataFactory.modificationInstant(),
                DocumentStatusDTO.CREATED
        );

        fixture.givenNoPriorActivity()
                .when(command)
                .expectException(CreatedGarageException.class)
                .expectExceptionMessage(containsString("document"));
    }

    @Test
    void shouldSetIdAfterEventSourcingHandler() {
        fixture.givenNoPriorActivity()
                .when(DocumentTestDataFactory.sampleCreateCommand())
                .expectState(aggregate -> assertEquals("doc-1", aggregate.getIdDocument()));
    }

    @Test
    void shouldSetNomDocumentAfterEventSourcingHandler() {
        fixture.givenNoPriorActivity()
                .when(DocumentTestDataFactory.sampleCreateCommand())
                .expectState(aggregate -> assertEquals("DOC-FACTURE-2026", aggregate.getNomDocument()));
    }

    @Test
    void shouldSetTitreDocumentAfterEventSourcingHandler() {
        fixture.givenNoPriorActivity()
                .when(DocumentTestDataFactory.sampleCreateCommand())
                .expectState(aggregate -> assertEquals("Facture Avril", aggregate.getTitreDocument()));
    }

    @Test
    void shouldSetEmetteurAfterEventSourcingHandler() {
        fixture.givenNoPriorActivity()
                .when(DocumentTestDataFactory.sampleCreateCommand())
                .expectState(aggregate -> assertEquals("Service Compta", aggregate.getEmetteurDuDocument()));
    }

    @Test
    void shouldSetTypeDocumentAfterEventSourcingHandler() {
        fixture.givenNoPriorActivity()
                .when(DocumentTestDataFactory.sampleCreateCommand())
                .expectState(aggregate -> assertNotNull(aggregate.getTypeDocument()));
    }

    @Test
    void shouldSetCreationDateAfterEventSourcingHandler() {
        fixture.givenNoPriorActivity()
                .when(DocumentTestDataFactory.sampleCreateCommand())
                .expectState(aggregate -> assertEquals(DocumentTestDataFactory.creationInstant(), aggregate.getDateCreationDocument()));
    }

    @Test
    void shouldSetModificationDateAfterEventSourcingHandler() {
        fixture.givenNoPriorActivity()
                .when(DocumentTestDataFactory.sampleCreateCommand())
                .expectState(aggregate -> assertEquals(DocumentTestDataFactory.modificationInstant(), aggregate.getDateModificationDocument()));
    }

    @Test
    void shouldSetDocumentStatusAfterEventSourcingHandler() {
        fixture.givenNoPriorActivity()
                .when(DocumentTestDataFactory.sampleCreateCommand())
                .expectState(aggregate -> assertEquals(DocumentStatusDTO.CREATED, aggregate.getDocumentStatus()));
    }
}
