package fr.cdrochon.smamonolithe.dossier.query.dtos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Cette classe contient uniquement des tests unitaires.
 */
class DossierStatusDTOTest {

    @Test
    void shouldParseEnumIgnoringCase() {
        assertEquals(DossierStatusDTO.ARCHIVE, DossierStatusDTO.forValue("archive"));
        assertEquals(DossierStatusDTO.REOUVERT, DossierStatusDTO.forValue("ReOuVeRt"));
    }

    @Test
    void shouldFallbackToOuvertWhenValueIsUnknown() {
        assertEquals(DossierStatusDTO.OUVERT, DossierStatusDTO.forValue("inconnu"));
    }
}

