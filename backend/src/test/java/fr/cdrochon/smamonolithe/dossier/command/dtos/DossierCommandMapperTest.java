package fr.cdrochon.smamonolithe.dossier.command.dtos;

import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import org.junit.jupiter.api.Test;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;

class DossierCommandMapperTest {

    @Test
    void shouldConvertVehiculeDtoToVehicule() {
        VehiculeCommandDTO dto = sampleVehiculeCommandDto();

        Vehicule vehicule = DossierCommandMapper.convertVehiculeDtoToVehicule(dto);

        assertNotNull(vehicule);
        assertEquals(dto.getId(), vehicule.getId());
        assertEquals(dto.getImmatriculationVehicule(), vehicule.getImmatriculationVehicule());
        assertEquals(dto.getDateMiseEnCirculationVehicule(), vehicule.getDateMiseEnCirculationVehicule());
        assertEquals(dto.getVehiculeStatus(), vehicule.getVehiculeStatus());
    }

    @Test
    void shouldConvertClientDtoToClient() {
        ClientCommandDTO dto = sampleClientCommandDto();

        Client client = DossierCommandMapper.convertClientDtoToClient(dto);

        assertNotNull(client);
        assertEquals(dto.getId(), client.getId());
        assertEquals(dto.getNomClient(), client.getNomClient());
        assertEquals(dto.getPrenomClient(), client.getPrenomClient());
        assertEquals(dto.getMailClient(), client.getMailClient());
        assertEquals(dto.getTelClient(), client.getTelClient());
        assertEquals(dto.getAdresse().getRue(), client.getAdresse().getRue());
        assertEquals(dto.getAdresse().getPays(), client.getAdresse().getPays());
        assertEquals(dto.getClientStatus(), client.getClientStatus());
    }

    @Test
    void shouldConvertClientToClientDto() {
        Client client = sampleClientEntity();

        ClientCommandDTO dto = DossierCommandMapper.convertClientToClientDTO(client);

        assertNotNull(dto);
        assertEquals(client.getId(), dto.getId());
        assertEquals(client.getNomClient(), dto.getNomClient());
        assertEquals(client.getAdresse().getVille(), dto.getAdresse().getVille());
        assertEquals(client.getClientStatus(), dto.getClientStatus());
    }

    @Test
    void shouldConvertVehiculeToVehiculeDto() {
        Vehicule vehicule = sampleVehiculeEntity();

        VehiculeCommandDTO dto = DossierCommandMapper.convertVehiculeToVehiculeDTO(vehicule);

        assertNotNull(dto);
        assertEquals(vehicule.getId(), dto.getId());
        assertEquals(vehicule.getImmatriculationVehicule(), dto.getImmatriculationVehicule());
        assertEquals(vehicule.getDateMiseEnCirculationVehicule(), dto.getDateMiseEnCirculationVehicule());
        assertEquals(vehicule.getVehiculeStatus(), dto.getVehiculeStatus());
    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        assertNull(DossierCommandMapper.convertVehiculeDtoToVehicule(null));
        assertNull(DossierCommandMapper.convertClientDtoToClient(null));
        assertNull(DossierCommandMapper.convertClientToClientDTO(null));
        assertNull(DossierCommandMapper.convertVehiculeToVehiculeDTO(null));
    }
}

