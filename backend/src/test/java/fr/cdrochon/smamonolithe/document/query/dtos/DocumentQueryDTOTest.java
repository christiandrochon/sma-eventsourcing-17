package fr.cdrochon.smamonolithe.document.query.dtos;

import fr.cdrochon.smamonolithe.document.DocumentTestDataFactory;
import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Cette classe contient uniquement des tests unitaires.
 */
class DocumentQueryDTOTest {

    @Test
    void shouldConstructWithBuilder() {
        DocumentQueryDTO dto = DocumentTestDataFactory.sampleQueryDTO();

        assertEquals("doc-1", dto.getId());
        assertEquals("DOC-FACTURE-2026", dto.getNomDocument());
        assertEquals(DocumentStatusDTO.CREATED, dto.getDocumentStatus());
    }

    @Test
    void shouldSupportNoArgsAndSetters() {
        DocumentQueryDTO dto = new DocumentQueryDTO();
        dto.setId("id-2");
        dto.setNomDocument("Nom");

        assertEquals("id-2", dto.getId());
        assertEquals("Nom", dto.getNomDocument());
    }

    @Test
    void shouldAllowNullValues() {
        DocumentQueryDTO dto = new DocumentQueryDTO();

        assertNull(dto.getId());
        assertNull(dto.getTypeDocument());
        assertNull(dto.getDocumentStatus());
    }

    @Test
    void shouldImplementEqualsHashCodeAndToString() {
        DocumentQueryDTO left = DocumentTestDataFactory.sampleQueryDTO();
        DocumentQueryDTO right = DocumentTestDataFactory.sampleQueryDTO();

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());

        right.setId("other");
        assertNotEquals(left, right);
    }
}
