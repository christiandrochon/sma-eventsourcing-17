package fr.cdrochon.smamonolithe.dossier.events;

import org.junit.jupiter.api.Test;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DossierCreatedEventTest {

    @Test
    void shouldExposeAllEventFields() {
        DossierCreatedEvent event = sampleDossierCreatedEvent();

        assertEquals(DOSSIER_ID, event.getId());
        assertEquals("DOSSIER-001", event.getNomDossier());
        assertEquals(CREATED_AT, event.getDateCreationDossier());
        assertEquals(UPDATED_AT, event.getDateModificationDossier());
        assertEquals(CLIENT_ID, event.getClientId());
        assertEquals(VEHICULE_ID, event.getVehiculeId());
    }
}

