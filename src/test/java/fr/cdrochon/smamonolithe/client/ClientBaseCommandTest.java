package fr.cdrochon.smamonolithe.client.command.commands;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientBaseCommandTest {

    @Test
    void shouldStoreIdCorrectly() {
        ClientBaseCommand<String> cmd = new ClientBaseCommand<>("id-123") {};
        assertEquals("id-123", cmd.getId());
    }

    @Test
    void shouldAcceptNullId() {
        ClientBaseCommand<String> cmd = new ClientBaseCommand<>(null) {};
        assertNull(cmd.getId());
    }

    @Test
    void shouldAcceptIntegerId() {
        ClientBaseCommand<Integer> cmd = new ClientBaseCommand<>(42) {};
        assertEquals(42, cmd.getId());
    }

    @Test
    void shouldReturnSameIdReference() {
        String id = "ref-id";
        ClientBaseCommand<String> cmd = new ClientBaseCommand<>(id) {};
        assertSame(id, cmd.getId());
    }
}