package fr.cdrochon.smamonolithe.document.query.entities;

import fr.cdrochon.smamonolithe.document.DocumentTestDataFactory;
import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentEntityTest {

    @Test
    void shouldBuildEntityWithBuilder() {
        Document document = DocumentTestDataFactory.sampleEntity();

        assertEquals("doc-1", document.getId());
        assertEquals("DOC-FACTURE-2026", document.getNomDocument());
        assertEquals(DocumentStatusDTO.CREATED, document.getDocumentStatus());
    }

    @Test
    void shouldSupportNoArgsConstructorAndSetters() {
        Document document = new Document();
        document.setId("id-2");
        document.setNomDocument("Nom");

        assertEquals("id-2", document.getId());
        assertEquals("Nom", document.getNomDocument());
    }

    @Test
    void shouldSupportAllArgsConstructor() {
        Document document = new Document(
                "id-3",
                "N",
                "T",
                "E",
                DocumentTestDataFactory.sampleTypeDocument(),
                DocumentTestDataFactory.creationInstant(),
                DocumentTestDataFactory.modificationInstant(),
                DocumentStatusDTO.ARCHIVED
        );

        assertEquals("id-3", document.getId());
        assertEquals("T", document.getTitreDocument());
        assertEquals(DocumentStatusDTO.ARCHIVED, document.getDocumentStatus());
    }

    @Test
    void shouldAllowNullDefaults() {
        Document document = new Document();

        assertNull(document.getId());
        assertNull(document.getTypeDocument());
        assertNull(document.getDocumentStatus());
    }

    @Test
    void shouldNotChangeOtherFieldsWhenOneSetterCalled() {
        Document document = DocumentTestDataFactory.sampleEntity();
        document.setNomDocument("CHANGED");

        assertEquals("CHANGED", document.getNomDocument());
        assertEquals("doc-1", document.getId()); // unchanged
        assertEquals(DocumentStatusDTO.CREATED, document.getDocumentStatus()); // unchanged
    }

    @Test
    void shouldProvideNonNullToString() {
        Document document = DocumentTestDataFactory.sampleEntity();

        String text = document.toString();

        assertNotNull(text);
        assertTrue(text.contains("doc-1"));
    }
}
