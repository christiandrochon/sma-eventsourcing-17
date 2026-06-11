package fr.cdrochon.smamonolithe.dossier.command.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Cette classe contient uniquement des tests unitaires.
 */
class DossierBaseCommandTest {

    @Test
    void shouldKeepIdentifier() {
        DossierBaseCommand<String> command = new DossierBaseCommand<>("id-1");

        assertEquals("id-1", command.getId());
    }
}

