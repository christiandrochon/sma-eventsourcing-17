package fr.cdrochon.smamonolithe.logging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class BackendLoggingStartupProbe implements ApplicationRunner {

    private static final Logger TECHNICAL_LOGGER = LoggerFactory.getLogger(BackendLoggingStartupProbe.class);

    @Value("${logging.file.path:backend/logs}")
    private String loggingPath;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path rootPath = Path.of(loggingPath).toAbsolutePath().normalize();
        Path metierPath = rootPath.resolve("metier");
        Path techniquePath = rootPath.resolve("technique");
        Path businessLogFile = metierPath.resolve("business.log");
        Path technicalLogFile = techniquePath.resolve("technical.log");
        Path securityLogFile = rootPath.resolve("security.log");

        Files.createDirectories(metierPath);
        Files.createDirectories(techniquePath);

        if (Files.notExists(businessLogFile)) {
            Files.createFile(businessLogFile);
        }

        if (Files.notExists(technicalLogFile)) {
            Files.createFile(technicalLogFile);
        }

        if (Files.notExists(securityLogFile)) {
            Files.createFile(securityLogFile);
        }

        BusinessLoggers.business().info("BIZ_STARTUP logDir={}", metierPath);
        TECHNICAL_LOGGER.warn("TECH_STARTUP logFile={}", technicalLogFile);
        BackendSecurityLoggers.security().warn("SEC_STARTUP logFile={}", securityLogFile);
    }
}

