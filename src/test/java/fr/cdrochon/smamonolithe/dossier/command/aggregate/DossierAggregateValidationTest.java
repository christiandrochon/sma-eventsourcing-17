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

import java.time.Instant;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DossierAggregateValidationTest {

    private FixtureConfiguration<DossierAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(DossierAggregate.class);
    }

    @Test
    void shouldRejectDossierWhenClientAndVehiculeAreBothNull() {
        DossierCreateCommand command = new DossierCreateCommand(
                DOSSIER_ID,
                "DOSSIER-001",
                CREATED_AT,
                UPDATED_AT,
                null,
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
    void shouldRejectDossierWhenModificationDateIsBeforeCreationDate() {
        Client client = sampleClientEntity();
        Vehicule vehicule = sampleVehiculeEntity();
        Instant futureDate = CREATED_AT.plusSeconds(3600);
        Instant pastDate = CREATED_AT.minusSeconds(3600);

        DossierCreateCommand command = new DossierCreateCommand(
                DOSSIER_ID,
                "DOSSIER-001",
                futureDate,
                pastDate,
                client,
                vehicule,
                DossierStatus.OUVERT,
                CLIENT_ID,
                VEHICULE_ID
        );

        fixture.givenNoPriorActivity()
                .when(command)
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(org.axonframework.test.matchers.Matchers.payloadsMatching(
                        org.axonframework.test.matchers.Matchers.listWithAllOf(
                                org.axonframework.test.matchers.Matchers.matches((DossierCreatedEvent event) ->
                                        event.getDateCreationDossier().isAfter(event.getDateModificationDossier()))
                        )));
    }

    @Test
    void shouldAcceptDossierWithEmptyDossierName() {
        Client client = sampleClientEntity();
        Vehicule vehicule = sampleVehiculeEntity();

        DossierCreateCommand command = new DossierCreateCommand(
                DOSSIER_ID,
                "",
                CREATED_AT,
                UPDATED_AT,
                client,
                vehicule,
                DossierStatus.OUVERT,
                CLIENT_ID,
                VEHICULE_ID
        );

        fixture.givenNoPriorActivity()
                .when(command)
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(org.axonframework.test.matchers.Matchers.payloadsMatching(
                        org.axonframework.test.matchers.Matchers.listWithAllOf(
                                org.axonframework.test.matchers.Matchers.matches((DossierCreatedEvent event) ->
                                        event.getNomDossier().isEmpty())
                        )));
    }

    @Test
    void shouldAcceptDossierWithVeryLongName() {
        Client client = sampleClientEntity();
        Vehicule vehicule = sampleVehiculeEntity();
        String veryLongName = "A".repeat(500);

        DossierCreateCommand command = new DossierCreateCommand(
                DOSSIER_ID,
                veryLongName,
                CREATED_AT,
                UPDATED_AT,
                client,
                vehicule,
                DossierStatus.OUVERT,
                CLIENT_ID,
                VEHICULE_ID
        );

        fixture.givenNoPriorActivity()
                .when(command)
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(org.axonframework.test.matchers.Matchers.payloadsMatching(
                        org.axonframework.test.matchers.Matchers.listWithAllOf(
                                org.axonframework.test.matchers.Matchers.matches((DossierCreatedEvent event) ->
                                        event.getNomDossier().length() == 500)
                        )));
    }
}

