package fr.cdrochon.smamonolithe.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cette classe contient uniquement des tests unitaires.
 */
class BackendLoggingStartupProbeTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldCreateExpectedLogFilesAtStartup() throws Exception {
        BackendLoggingStartupProbe probe = new BackendLoggingStartupProbe();
        ReflectionTestUtils.setField(probe, "loggingPath", tempDir.toString());

        probe.run(new DefaultApplicationArguments(new String[]{}));

        assertTrue(Files.exists(tempDir.resolve("metier/business.log")));
        assertTrue(Files.exists(tempDir.resolve("technique/technical.log")));
        assertTrue(Files.exists(tempDir.resolve("security.log")));
    }
}

