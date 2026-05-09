package fr.cdrochon.smamonolithe.logging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class BackendLoggingStartupProbe implements ApplicationRunner {

    @Value("${logging.file.path:backend/logs}")
    private String loggingPath;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path rootPath = Path.of(loggingPath).toAbsolutePath().normalize();
        Path metierPath = rootPath.resolve("metier");
        Path techniquePath = rootPath.resolve("technique");

        Files.createDirectories(metierPath);
        Files.createDirectories(techniquePath);

        BusinessLoggers.business().info("BIZ_STARTUP logDir={}", metierPath);
    }
}

