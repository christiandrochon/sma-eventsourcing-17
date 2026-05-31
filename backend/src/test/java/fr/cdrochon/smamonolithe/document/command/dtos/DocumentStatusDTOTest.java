package fr.cdrochon.smamonolithe.document.command.dtos;

import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cette classe contient uniquement des tests unitaires.
 */
class DocumentStatusDTOTest {

    @Test
    void shouldContainCreatedStatus() {
        assertEquals(DocumentStatusDTO.CREATED, DocumentStatusDTO.valueOf("CREATED"));
    }

    @Test
    void shouldContainArchivedStatus() {
        assertEquals(DocumentStatusDTO.ARCHIVED, DocumentStatusDTO.valueOf("ARCHIVED"));
    }

    @Test
    void shouldContainVeryLongCompositeStatus() {
        assertEquals(
                DocumentStatusDTO.TO_BE_PAID_AND_SENT_AND_ACCEPTED_AND_COMPLETED_AND_ARCHIVED_AND_DELETED_AND_REFUSED,
                DocumentStatusDTO.valueOf("TO_BE_PAID_AND_SENT_AND_ACCEPTED_AND_COMPLETED_AND_ARCHIVED_AND_DELETED_AND_REFUSED")
        );
    }

    @Test
    void shouldExposeStableEnumOrder() {
        assertTrue(DocumentStatusDTO.values().length >= 10);
    }
}
