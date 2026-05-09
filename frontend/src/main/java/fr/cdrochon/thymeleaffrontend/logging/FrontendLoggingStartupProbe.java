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
        Path rootPath = Path.of(loggingPath).toAbsolutePath().normalize();
        Path metierPath = rootPath.resolve("metier");
        Path techniquePath = rootPath.resolve("technique");
        Path securityPath = rootPath.resolve("securite");
        Path accessLogFile = metierPath.resolve("ui-access.log");
        Path errorLogFile = techniquePath.resolve("ui-error.log");
        Path technicalLogFile = techniquePath.resolve("ui-technical.log");
        Path businessLogFile = metierPath.resolve("ui-business.log");
        Path securityLogFile = securityPath.resolve("ui-security.log");

        Files.createDirectories(metierPath);
        Files.createDirectories(techniquePath);
        Files.createDirectories(securityPath);

        if (Files.notExists(accessLogFile)) {
            Files.createFile(accessLogFile);
        }

        if (Files.notExists(errorLogFile)) {
            Files.createFile(errorLogFile);
        }

        if (Files.notExists(technicalLogFile)) {
            Files.createFile(technicalLogFile);
        }

        if (Files.notExists(businessLogFile)) {
            Files.createFile(businessLogFile);
        }

        if (Files.notExists(securityLogFile)) {
            Files.createFile(securityLogFile);
        }

        FrontendLoggers.access().info("UI_ACCESS_STARTUP logDir={}", metierPath);
        FrontendLoggers.error().warn("UI_ERROR_STARTUP logFile={}", errorLogFile);
        FrontendLoggers.tech().info("UI_TECH_STARTUP logDir={}", techniquePath);
        FrontendLoggers.business().info("UI_BUSINESS_STARTUP logFile={}", businessLogFile);
        FrontendSecurityLoggers.security().warn("SEC_FRONTEND_STARTUP logFile={}", securityLogFile);
    }
}

