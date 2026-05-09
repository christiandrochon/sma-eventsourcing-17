package fr.cdrochon.smamonolithe.dossier.query.mapper;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import org.junit.jupiter.api.Test;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.sampleDossierEntity;
import static org.junit.jupiter.api.Assertions.*;

class DossierQueryMapperTest {

    @Test
    void shouldConvertDossierToDossierDto() {
        Dossier dossier = sampleDossierEntity();

        DossierQueryDTO dto = DossierQueryMapper.convertDossierToDossierDTO(dossier);

        assertNotNull(dto);
        assertEquals(dossier.getId(), dto.getId());
        assertEquals(dossier.getNomDossier(), dto.getNomDossier());
        assertEquals(dossier.getDateCreationDossier(), dto.getDateCreationDossier());
        assertEquals(dossier.getDateModificationDossier(), dto.getDateModificationDossier());
        assertEquals(dossier.getDossierStatus(), dto.getDossierStatus());

        assertNotNull(dto.getClient());
        assertEquals(dossier.getClient().getId(), dto.getClient().getId());
        assertNull(dto.getClient().getVehicule());

        assertNotNull(dto.getVehicule());
        assertEquals(dossier.getVehicule().getId(), dto.getVehicule().getId());
        assertNull(dto.getVehicule().getClient());
    }

    @Test
    void shouldCreateClientDtoFromDossier() {
        Dossier dossier = sampleDossierEntity();

        ClientQueryDTO clientDto = DossierQueryMapper.creeClientDepuisDossier(dossier);

        assertNotNull(clientDto);
        assertEquals(dossier.getClient().getId(), clientDto.getId());
        assertEquals(dossier.getClient().getAdresse().getRue(), clientDto.getAdresse().getRue());
        assertNull(clientDto.getVehicule());
    }

    @Test
    void shouldCreateVehiculeDtoFromDossier() {
        Dossier dossier = sampleDossierEntity();

        VehiculeQueryDTO vehiculeDto = DossierQueryMapper.creeVehiculeDtoDepuisDossier(dossier);

        assertNotNull(vehiculeDto);
        assertEquals(dossier.getVehicule().getId(), vehiculeDto.getId());
        assertEquals(dossier.getVehicule().getImmatriculationVehicule(), vehiculeDto.getImmatriculationVehicule());
        assertNull(vehiculeDto.getClient());
    }

    @Test
    void shouldReturnNullWhenDossierIsNull() {
        assertNull(DossierQueryMapper.convertDossierToDossierDTO(null));
        assertNull(DossierQueryMapper.creeClientDepuisDossier(null));
        assertNull(DossierQueryMapper.creeVehiculeDtoDepuisDossier(null));
    }
}

