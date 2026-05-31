package fr.cdrochon.smamonolithe.document.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Cette classe contient uniquement des tests unitaires.
 */
class DocumentBaseEventTest {

    @Test
    void shouldStoreId() {
        DocumentBaseEvent<String> event = new DocumentBaseEvent<>("doc-1");

        assertEquals("doc-1", event.getId());
    }

    @Test
    void shouldAllowNullId() {
        DocumentBaseEvent<String> event = new DocumentBaseEvent<>(null);

        assertNull(event.getId());
    }

    @Test
    void shouldSupportDifferentIdType() {
        DocumentBaseEvent<Integer> event = new DocumentBaseEvent<>(99);

        assertEquals(99, event.getId());
    }
}
