package fr.cdrochon.smamonolithe.client;

import fr.cdrochon.smamonolithe.client.command.commands.ClientCreateCommand;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import fr.cdrochon.smamonolithe.client.query.entities.Pays;
import org.junit.jupiter.api.Test;

import static fr.cdrochon.smamonolithe.client.ClientTestDataFactory.sampleAdresseDTO;
import static org.junit.jupiter.api.Assertions.*;

class ClientCreateCommandTest {

    @Test
    void shouldCreateCommandWithAllFields() {
        ClientAdresseDTO dto = sampleAdresseDTO();
        ClientCreateCommand cmd = new ClientCreateCommand("id-1", "Dupont", "Jean", "j@mail.com", "0600000000", dto);

        assertEquals("id-1", cmd.getId());
        assertEquals("Dupont", cmd.getNomClient());
        assertEquals("Jean", cmd.getPrenomClient());
        assertEquals("j@mail.com", cmd.getMailClient());
        assertEquals("0600000000", cmd.getTelClient());
    }

    @Test
    void shouldCopyAdresseClientFromDTO() {
        ClientAdresseDTO dto = sampleAdresseDTO();
        ClientCreateCommand cmd = new ClientCreateCommand("id-1", "A", "B", "c@d.fr", "0", dto);

        AdresseClient adresse = cmd.getAdresseClient();
        assertNotNull(adresse);
        assertEquals("Rue de la Paix", adresse.getRue());
        assertEquals("75001", adresse.getCp());
        assertEquals(Pays.FRANCE, adresse.getPays());
    }

    @Test
    void shouldHaveImmutableAdresseCopy() {
        ClientAdresseDTO dto = sampleAdresseDTO();
        ClientCreateCommand cmd = new ClientCreateCommand("id-1", "A", "B", "c@d.fr", "0", dto);
        // Modifie le DTO original → ne doit pas affecter la commande
        dto.setRue("Rue Modifiée");
        assertEquals("Rue de la Paix", cmd.getAdresseClient().getRue());
    }

    @Test
    void shouldAcceptNullNomClient() {
        ClientAdresseDTO dto = sampleAdresseDTO();
        ClientCreateCommand cmd = new ClientCreateCommand("id-1", null, "Jean", "j@mail.com", "0600000000", dto);
        assertNull(cmd.getNomClient());
    }

    @Test
    void shouldAcceptNullPrenomClient() {
        ClientAdresseDTO dto = sampleAdresseDTO();
        ClientCreateCommand cmd = new ClientCreateCommand("id-1", "Dupont", null, "j@mail.com", "0600000000", dto);
        assertNull(cmd.getPrenomClient());
    }

    @Test
    void shouldAcceptUnicodeNames() {
        ClientAdresseDTO dto = sampleAdresseDTO();
        ClientCreateCommand cmd = new ClientCreateCommand("id-1", "Ñoño", "Ébène", "e@mail.com", "0", dto);
        assertEquals("Ñoño", cmd.getNomClient());
        assertEquals("Ébène", cmd.getPrenomClient());
    }
}