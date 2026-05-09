package fr.cdrochon.smamonolithe.dossier.command.aggregate;

import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.dossier.command.commands.DossierCreateCommand;
import fr.cdrochon.smamonolithe.dossier.events.DossierCreatedEvent;
import fr.cdrochon.smamonolithe.dossier.query.entities.DossierStatus;
import fr.cdrochon.smamonolithe.garage.command.exceptions.CreatedGarageException;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.*;
import static org.axonframework.test.matchers.Matchers.*;

class DossierAggregateTest {

    private FixtureConfiguration<DossierAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(DossierAggregate.class);
    }

    @Test
    void shouldPublishCreatedEventWhenCommandIsValid() {
        DossierCreateCommand command = sampleDossierCreateCommand();

        fixture.givenNoPriorActivity()
                .when(command)
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(payloadsMatching(exactSequenceOf(matches((DossierCreatedEvent event) ->
                        event.getId().equals(command.getId())
                                && event.getNomDossier().equals(command.getNomDossier())
                                && event.getClient().getId().equals(command.getClient().getId())
                                && event.getVehicule().getId().equals(command.getVehicule().getId())
                                && event.getDossierStatus() == DossierStatus.OUVERT
                ))));
    }

    @Test
    void shouldRejectCommandWhenClientIsMissing() {
        Vehicule vehicule = sampleVehiculeEntity();
        DossierCreateCommand command = new DossierCreateCommand(
                DOSSIER_ID,
                "DOSSIER-001",
                CREATED_AT,
                UPDATED_AT,
                null,
                vehicule,
                DossierStatus.OUVERT,
                CLIENT_ID,
                VEHICULE_ID
        );

        fixture.givenNoPriorActivity()
                .when(command)
                .expectException(CreatedGarageException.class);
    }

    @Test
    void shouldRejectCommandWhenVehiculeIsMissing() {
        Client client = sampleClientEntity();
        DossierCreateCommand command = new DossierCreateCommand(
                DOSSIER_ID,
                "DOSSIER-001",
                CREATED_AT,
                UPDATED_AT,
                client,
                null,
                DossierStatus.OUVERT,
                CLIENT_ID,
                VEHICULE_ID
        );

        fixture.givenNoPriorActivity()
                .when(command)
                .expectException(CreatedGarageException.class);
    }

    @Test
    void shouldRehydrateStateFromCreatedEvent() {
        DossierCreatedEvent event = sampleDossierCreatedEvent();
        DossierAggregate aggregate = new DossierAggregate();

        aggregate.on(event);

        org.junit.jupiter.api.Assertions.assertEquals(event.getId(), aggregate.getId());
        org.junit.jupiter.api.Assertions.assertEquals(event.getNomDossier(), aggregate.getNomDossier());
        org.junit.jupiter.api.Assertions.assertEquals(event.getDateCreationDossier(), aggregate.getDateCreationDossier());
        org.junit.jupiter.api.Assertions.assertEquals(event.getDateModificationDossier(), aggregate.getDateModificationDossier());
        org.junit.jupiter.api.Assertions.assertEquals(event.getClient().getId(), aggregate.getClient().getId());
        org.junit.jupiter.api.Assertions.assertEquals(event.getVehicule().getId(), aggregate.getVehicule().getId());
        org.junit.jupiter.api.Assertions.assertEquals(event.getDossierStatus(), aggregate.getDossierStatus());
    }
}

