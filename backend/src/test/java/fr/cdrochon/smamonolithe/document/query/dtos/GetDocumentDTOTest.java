package fr.cdrochon.smamonolithe.document.query.dtos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Cette classe contient uniquement des tests unitaires.
 */
class GetDocumentDTOTest {

    @Test
    void shouldCreateWithAllArgsConstructor() {
        GetDocumentDTO dto = new GetDocumentDTO("doc-1");

        assertEquals("doc-1", dto.getId());
    }

    @Test
    void shouldAllowNoArgsAndSetter() {
        GetDocumentDTO dto = new GetDocumentDTO();
        dto.setId("doc-2");

        assertEquals("doc-2", dto.getId());
    }

    @Test
    void shouldAllowNullId() {
        GetDocumentDTO dto = new GetDocumentDTO();

        assertNull(dto.getId());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        GetDocumentDTO left = new GetDocumentDTO("same");
        GetDocumentDTO right = new GetDocumentDTO("same");

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());

        right.setId("other");
        assertNotEquals(left, right);
    }
}
