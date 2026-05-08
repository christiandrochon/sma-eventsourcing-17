package fr.cdrochon.smamonolithe.document.command.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DocumentBaseCommandTest {

    @Test
    void shouldStoreStringId() {
        DocumentBaseCommand<String> command = new DocumentBaseCommand<>("id-123");
        assertEquals("id-123", command.getId());
    }

    @Test
    void shouldStoreIntegerId() {
        DocumentBaseCommand<Integer> command = new DocumentBaseCommand<>(42);
        assertEquals(42, command.getId());
    }

    @Test
    void shouldAllowNullId() {
        DocumentBaseCommand<String> command = new DocumentBaseCommand<>(null);
        assertNull(command.getId());
    }

    @Test
    void shouldKeepSameReferenceForIdObject() {
        String id = "same-ref";
        DocumentBaseCommand<String> command = new DocumentBaseCommand<>(id);
        assertSame(id, command.getId());
    }
}
