package fr.cdrochon.smamonolithe.dossier.command.commands;

import fr.cdrochon.smamonolithe.dossier.query.entities.DossierStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Cette classe contient uniquement des tests unitaires.
 */
class DossierCreateCommandTest {

    @Test
    void shouldExposeAllConstructorValues() {
        DossierCreateCommand command = sampleDossierCreateCommand();

        assertEquals(DOSSIER_ID, command.getId());
        assertEquals("DOSSIER-001", command.getNomDossier());
        assertEquals(CREATED_AT, command.getDateCreationDossier());
        assertEquals(UPDATED_AT, command.getDateModificationDossier());
        assertEquals(CLIENT_ID, command.getClientId());
        assertEquals(VEHICULE_ID, command.getVehiculeId());
        assertEquals(DossierStatus.OUVERT, command.getDossierStatus());
    }

    @Test
    void shouldReturn500FromExceptionHandler() {
        DossierCreateCommand command = sampleDossierCreateCommand();

        ResponseEntity<String> response = command.exceptionHandler(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("boom", response.getBody());
    }
}

