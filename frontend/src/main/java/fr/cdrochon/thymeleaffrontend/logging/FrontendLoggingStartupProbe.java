package fr.cdrochon.thymeleaffrontend.logging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FrontendLoggingStartupProbe implements ApplicationRunner {

    @Value("${logging.file.path:frontend/logs}")
    private String loggingPath;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path path = Path.of(loggingPath).toAbsolutePath().normalize();
        Files.createDirectories(path);
        FrontendLoggers.access().info("UI_ACCESS_STARTUP logDir={}", path);
        FrontendLoggers.tech().info("UI_TECH_STARTUP logDir={}", path);
    }
}

