package fr.cdrochon.smamonolithe.document.command.dtos;

import fr.cdrochon.smamonolithe.document.DocumentTestDataFactory;
import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Cette classe contient uniquement des tests unitaires.
 */
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
    void shouldSupportBuilder() {
        DocumentCommandDTO dto = DocumentCommandDTO.builder()
                .id("id-2")
                .nomDocument("Nom")
                .titreDocument("Titre")
                .emetteurDuDocument("Emetteur")
                .typeDocument(DocumentTestDataFactory.sampleTypeDocument())
                .dateCreationDocument(DocumentTestDataFactory.creationInstant())
                .dateModificationDocument(DocumentTestDataFactory.modificationInstant())
                .documentStatus(DocumentStatusDTO.ARCHIVED)
                .clientId("client-1")
                .build();

        assertEquals("id-2", dto.getId());
        assertEquals("Titre", dto.getTitreDocument());
        assertEquals(DocumentStatusDTO.ARCHIVED, dto.getDocumentStatus());
        assertEquals("client-1", dto.getClientId());
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
