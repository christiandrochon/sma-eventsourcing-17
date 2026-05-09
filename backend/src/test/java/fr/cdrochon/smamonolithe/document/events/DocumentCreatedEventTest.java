package fr.cdrochon.smamonolithe.document.events;

import fr.cdrochon.smamonolithe.document.DocumentTestDataFactory;
import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentCreatedEventTest {

    @Test
    void shouldExposeAllFieldsFromConstructor() {
        DocumentCreatedEvent event = DocumentTestDataFactory.sampleCreatedEvent();

        assertEquals("doc-1", event.getId());
        assertEquals("DOC-FACTURE-2026", event.getNomDocument());
        assertEquals("Facture Avril", event.getTitreDocument());
        assertEquals("Service Compta", event.getEmetteurDuDocument());
        assertEquals(DocumentTestDataFactory.sampleTypeDocument(), event.getTypeDocument());
        assertEquals(DocumentTestDataFactory.creationInstant(), event.getDateCreationDocument());
        assertEquals(DocumentTestDataFactory.modificationInstant(), event.getDateModificationDocument());
        assertEquals(DocumentStatusDTO.CREATED, event.getDocumentStatus());
    }

    @Test
    void shouldAllowNullOptionalFields() {
        DocumentCreatedEvent event = new DocumentCreatedEvent("id", "N", null, null, null, null, null, null);

        assertEquals("id", event.getId());
        assertEquals("N", event.getNomDocument());
        assertNull(null, event.getTitreDocument());
        assertNull(null, String.valueOf(event.getDocumentStatus()));
    }

    @Test
    void shouldKeepBaseEventInheritanceContract() {
        DocumentCreatedEvent event = DocumentTestDataFactory.sampleCreatedEvent();

        assertNotNull(event.getId());
    }
}
