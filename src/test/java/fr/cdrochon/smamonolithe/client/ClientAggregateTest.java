package fr.cdrochon.smamonolithe.client;

import fr.cdrochon.smamonolithe.client.command.aggregate.ClientAggregate;
import fr.cdrochon.smamonolithe.client.command.commands.ClientCreateCommand;
import fr.cdrochon.smamonolithe.client.events.ClientCreatedEvent;
import fr.cdrochon.smamonolithe.client.query.entities.ClientStatus;
import fr.cdrochon.smamonolithe.garage.command.exceptions.CreatedGarageException;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static fr.cdrochon.smamonolithe.client.ClientTestDataFactory.sampleAdresseDTO;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;

class ClientAggregateTest {

    private AggregateTestFixture<ClientAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(ClientAggregate.class);
    }

    @Test
    void shouldPublishClientCreatedEventOnValidCommand() {
        fixture.givenNoPriorActivity()
                .when(new ClientCreateCommand("id-1", "Dupont", "Jean", "j@mail.com", "0600000000", sampleAdresseDTO()))
                .expectSuccessfulHandlerExecution()
                .expectEventsMatching(org.axonframework.test.matchers.Matchers.sequenceOf(
                        org.axonframework.test.matchers.Matchers.messageWithPayload(instanceOf(ClientCreatedEvent.class))
                ));
    }

    @Test
    void shouldThrowExceptionWhenNomClientIsNull() {
        fixture.givenNoPriorActivity()
                .when(new ClientCreateCommand("id-1", null, "Jean", "j@mail.com", "0600000000", sampleAdresseDTO()))
                .expectException(CreatedGarageException.class);
    }

    @Test
    void shouldSetClientStatusToACTIF() {
        fixture.givenNoPriorActivity()
                .when(new ClientCreateCommand("id-1", "Dupont", "Jean", "j@mail.com", "0600000000", sampleAdresseDTO()))
                .expectState(aggregate -> {
                    assert aggregate.getClientStatus() == ClientStatus.ACTIF;
                });
    }

    @Test
    void shouldSetIdFromCommandAfterEvent() {
        fixture.givenNoPriorActivity()
                .when(new ClientCreateCommand("id-xyz", "Dupont", "Jean", "j@mail.com", "0600000000", sampleAdresseDTO()))
                .expectState(aggregate -> {
                    assert "id-xyz".equals(aggregate.getId());
                });
    }

    @Test
    void shouldSetNomClientFromCommandAfterEvent() {
        fixture.givenNoPriorActivity()
                .when(new ClientCreateCommand("id-1", "Martin", "Luc", "luc@mail.com", "0600000001", sampleAdresseDTO()))
                .expectState(aggregate -> {
                    assert "Martin".equals(aggregate.getNomClient());
                });
    }

    @Test
    void shouldSetMailClientFromCommandAfterEvent() {
        fixture.givenNoPriorActivity()
                .when(new ClientCreateCommand("id-1", "A", "B", "test@example.com", "0", sampleAdresseDTO()))
                .expectState(aggregate -> {
                    assert "test@example.com".equals(aggregate.getMailClient());
                });
    }

    @Test
    void shouldSetAdresseFromCommandAfterEvent() {
        fixture.givenNoPriorActivity()
                .when(new ClientCreateCommand("id-1", "A", "B", "c@d.fr", "0", sampleAdresseDTO()))
                .expectState(aggregate -> {
                    assert aggregate.getAdresseClient() != null;
                    assert "Paris".equals(aggregate.getAdresseClient().getVille());
                });
    }


    @Test
    void shouldThrowExceptionMessageContainsNomClient() {
        fixture.givenNoPriorActivity()
                .when(new ClientCreateCommand("id-1", null, "Jean", "j@mail.com", "0600000000", sampleAdresseDTO()))
                .expectException(CreatedGarageException.class)
                .expectExceptionMessage(containsString("nom du client"));
    }
}