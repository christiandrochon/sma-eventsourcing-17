package fr.cdrochon.smamonolithe.dossier.command.dtos;

import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.entities.ClientStatus;
import fr.cdrochon.smamonolithe.client.query.entities.Pays;
import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DossierCommandMapperEdgeCasesTest {

    @Test
    void shouldHandleVehiculeWithVeryLongImmatriculation() {
        VehiculeCommandDTO dto = VehiculeCommandDTO.builder()
                .id("vehicule-1")
                .immatriculationVehicule("A".repeat(100))
                .dateMiseEnCirculationVehicule(Instant.now())
                .vehiculeStatus(VehiculeStatus.EN_CIRCULATION)
                .build();

        Vehicule vehicule = DossierCommandMapper.convertVehiculeDtoToVehicule(dto);

        assertNotNull(vehicule);
        assertEquals(100, vehicule.getImmatriculationVehicule().length());
    }

    @Test
    void shouldHandleClientWithSpecialCharactersInName() {
        ClientCommandDTO dto = ClientCommandDTO.builder()
                .id("client-1")
                .nomClient("O'Brien-Müller & Co.")
                .prenomClient("Jean-François")
                .mailClient("test@example.com")
                .telClient("+33 6 00-00-00-00")
                .adresse(ClientAdresseDTO.builder()
                        .numeroDeRue("10B")
                        .rue("Rue de l'Église")
                        .complementAdresse("Apt. 5")
                        .cp("75001")
                        .ville("Paris")
                        .pays(Pays.FRANCE)
                        .build())
                .clientStatus(ClientStatus.ACTIF)
                .build();

        Client client = DossierCommandMapper.convertClientDtoToClient(dto);

        assertNotNull(client);
        assertEquals("O'Brien-Müller & Co.", client.getNomClient());
        assertEquals("Jean-François", client.getPrenomClient());
    }

    @Test
    void shouldHandleClientWithUnicodeCharactersInAddress() {
        ClientCommandDTO dto = ClientCommandDTO.builder()
                .id("client-1")
                .nomClient("Client")
                .prenomClient("Test")
                .mailClient("test@example.com")
                .telClient("0600000000")
                .adresse(ClientAdresseDTO.builder()
                        .numeroDeRue("10")
                        .rue("Rue de la Côte d'Azur")
                        .complementAdresse("Bâtiment Ès-élèves")
                        .cp("06000")
                        .ville("Nîmes")
                        .pays(Pays.FRANCE)
                        .build())
                .clientStatus(ClientStatus.ACTIF)
                .build();

        Client client = DossierCommandMapper.convertClientDtoToClient(dto);

        assertNotNull(client);
        assertNotNull(client.getAdresse());
        assertTrue(client.getAdresse().getRue().contains("Côte d'Azur"));
    }
}

