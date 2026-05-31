package fr.cdrochon.smamonolithe.dossier.dto;

import fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetDossierDTO;
import org.junit.jupiter.api.Test;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Cette classe contient uniquement des tests unitaires.
 */
class DossierDtoTest {

    @Test
    void shouldBuildAndReadDossierCommandDto() {
        DossierCommandDTO dto = sampleDossierCommandDto();

        assertEquals(DOSSIER_ID, dto.getId());
        assertEquals("DOSSIER-001", dto.getNomDossier());
        assertEquals(CLIENT_ID, dto.getClient().getId());
    }

    @Test
    void shouldBuildAndReadDossierQueryDto() {
        DossierQueryDTO dto = DossierQueryDTO.builder()
                .id(DOSSIER_ID)
                .nomDossier("DOSSIER-001")
                .dateCreationDossier(CREATED_AT)
                .dateModificationDossier(UPDATED_AT)
                .dossierStatus(fr.cdrochon.smamonolithe.dossier.query.entities.DossierStatus.OUVERT)
                .build();

        assertEquals(DOSSIER_ID, dto.getId());
        assertEquals("DOSSIER-001", dto.getNomDossier());
    }

    @Test
    void shouldReadGetDossierDto() {
        GetDossierDTO dto = new GetDossierDTO(DOSSIER_ID);

        assertEquals(DOSSIER_ID, dto.getId());
    }
}

