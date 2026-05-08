package fr.cdrochon.smamonolithe.dossier.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DossierBaseEventTest {

    @Test
    void shouldKeepIdentifier() {
        DossierBaseEvent<String> event = new DossierBaseEvent<>("id-1");

        assertEquals("id-1", event.getId());
    }
}

