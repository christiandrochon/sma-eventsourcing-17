package fr.cdrochon.smamonolithe.document.command.dtos;

import fr.cdrochon.smamonolithe.document.DocumentTestDataFactory;
import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DocumentCommandDTOTest {

    @Test
    void shouldBuildDtoWithBuilder() {
        DocumentCommandDTO dto = DocumentTestDataFactory.sampleCommandDTO();

        assertEquals("doc-1", dto.getId());
        assertEquals("DOC-FACTURE-2026", dto.getNomDocument());
        assertEquals(DocumentStatusDTO.CREATED, dto.getDocumentStatus());
    }

    @Test
    void shouldSetAndGetFieldsWithSetters() {
        DocumentCommandDTO dto = new DocumentCommandDTO();
        dto.setId("doc-x");
        dto.setNomDocument("N");

        assertEquals("doc-x", dto.getId());
        assertEquals("N", dto.getNomDocument());
    }

    @Test
    void shouldSupportAllArgsConstructor() {
        DocumentCommandDTO dto = new DocumentCommandDTO(
                "id-2",
                "Nom",
                "Titre",
                "Emetteur",
                DocumentTestDataFactory.sampleTypeDocument(),
                DocumentTestDataFactory.creationInstant(),
                DocumentTestDataFactory.modificationInstant(),
                DocumentStatusDTO.ARCHIVED
        );

        assertEquals("id-2", dto.getId());
        assertEquals("Titre", dto.getTitreDocument());
        assertEquals(DocumentStatusDTO.ARCHIVED, dto.getDocumentStatus());
    }

    @Test
    void shouldAllowNullFieldsWithNoArgsConstructor() {
        DocumentCommandDTO dto = new DocumentCommandDTO();

        assertNull(dto.getId());
        assertNull(dto.getNomDocument());
        assertNull(dto.getDocumentStatus());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        DocumentCommandDTO left = DocumentTestDataFactory.sampleCommandDTO();
        DocumentCommandDTO right = DocumentTestDataFactory.sampleCommandDTO();

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());

        right.setId("different");
        assertNotEquals(left, right);
    }
}
