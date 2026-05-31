package fr.cdrochon.smamonolithe.document.query.mapper;

import fr.cdrochon.smamonolithe.document.DocumentTestDataFactory;
import fr.cdrochon.smamonolithe.document.query.dtos.DocumentQueryDTO;
import fr.cdrochon.smamonolithe.document.query.entities.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Cette classe contient uniquement des tests unitaires.
 */
class DocumentQueryMapperTest {

    @Test
    void shouldReturnNullWhenConvertingNullEntity() {
        assertNull(DocumentQueryMapper.convertDocumentToDocumentDTO(null));
    }

    @Test
    void shouldConvertEntityToDto() {
        Document entity = DocumentTestDataFactory.sampleEntity();

        DocumentQueryDTO dto = DocumentQueryMapper.convertDocumentToDocumentDTO(entity);

        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getNomDocument(), dto.getNomDocument());
        assertEquals(entity.getTitreDocument(), dto.getTitreDocument());
        assertEquals(entity.getEmetteurDuDocument(), dto.getEmetteurDuDocument());
        assertEquals(entity.getTypeDocument(), dto.getTypeDocument());
        assertEquals(entity.getDateCreationDocument(), dto.getDateCreationDocument());
        assertEquals(entity.getDateModificationDocument(), dto.getDateModificationDocument());
        assertEquals(entity.getDocumentStatus(), dto.getDocumentStatus());
    }

    @Test
    void shouldReturnNullWhenConvertingNullDto() {
        assertNull(DocumentQueryMapper.convertDocumentDTOToDocument(null));
    }

    @Test
    void shouldConvertDtoToEntity() {
        DocumentQueryDTO dto = DocumentTestDataFactory.sampleQueryDTO();

        Document entity = DocumentQueryMapper.convertDocumentDTOToDocument(dto);

        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getNomDocument(), entity.getNomDocument());
        assertEquals(dto.getTitreDocument(), entity.getTitreDocument());
        assertEquals(dto.getEmetteurDuDocument(), entity.getEmetteurDuDocument());
        assertEquals(dto.getTypeDocument(), entity.getTypeDocument());
        assertEquals(dto.getDateCreationDocument(), entity.getDateCreationDocument());
        assertEquals(dto.getDateModificationDocument(), entity.getDateModificationDocument());
        assertEquals(dto.getDocumentStatus(), entity.getDocumentStatus());
    }

    @Test
    void shouldPreserveNullFieldsInEntityToDto() {
        Document entity = new Document();
        entity.setId("doc-null");

        DocumentQueryDTO dto = DocumentQueryMapper.convertDocumentToDocumentDTO(entity);

        assertNull(dto.getNomDocument());
        assertNull(dto.getTypeDocument());
        assertNull(dto.getDocumentStatus());
    }

    @Test
    void shouldPreserveNullFieldsInDtoToEntity() {
        DocumentQueryDTO dto = new DocumentQueryDTO();
        dto.setId("doc-null");

        Document entity = DocumentQueryMapper.convertDocumentDTOToDocument(dto);

        assertNull(entity.getNomDocument());
        assertNull(entity.getTypeDocument());
        assertNull(entity.getDocumentStatus());
    }

    @Test
    void shouldCreateDistinctTargetInstances() {
        DocumentQueryDTO dto = DocumentTestDataFactory.sampleQueryDTO();

        Document one = DocumentQueryMapper.convertDocumentDTOToDocument(dto);
        Document two = DocumentQueryMapper.convertDocumentDTOToDocument(dto);

        assertNotSame(one, two);
    }
}
