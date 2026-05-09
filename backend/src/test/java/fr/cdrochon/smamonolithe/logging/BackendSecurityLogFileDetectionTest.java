package fr.cdrochon.smamonolithe.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BackendSecurityLogFileDetectionTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldWriteSecuritySignalsIntoSecurityLogFile() throws Exception {
        Path securityLogPath = tempDir.resolve("security.log");

        Logger securityLogger = (Logger) LoggerFactory.getLogger("SECURITY");
        LoggerContext context = securityLogger.getLoggerContext();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%msg%n");
        encoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setName("TEST_SECURITY_FILE_APPENDER");
        fileAppender.setFile(securityLogPath.toString());
        fileAppender.setEncoder(encoder);
        fileAppender.setAppend(false);
        fileAppender.start();

        Level previousLevel = securityLogger.getLevel();
        securityLogger.setLevel(Level.WARN);
        securityLogger.addAppender(fileAppender);

        try {
            TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();

            // Faute volontaire n°1: methode suspecte -> SEC_HTTP_ANOMALOUS_REQUEST
            MockServerWebExchange anomalousExchange = MockServerWebExchange.from(
                    MockServerHttpRequest.method(HttpMethod.DELETE, "/queries/vehicules").build()
            );
            WebFilterChain okChain = exchange -> {
                exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(200));
                return Mono.empty();
            };
            StepVerifier.create(filter.filter(anomalousExchange, okChain)).verifyComplete();

            // Faute volontaire n°2: acces refuse -> SEC_HTTP_ACCESS_DENIED
            MockServerWebExchange deniedExchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/queries/secure").build()
            );
            WebFilterChain deniedChain = exchange -> {
                exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(403));
                return Mono.empty();
            };
            StepVerifier.create(filter.filter(deniedExchange, deniedChain)).verifyComplete();
        } finally {
            securityLogger.detachAppender(fileAppender);
            securityLogger.setLevel(previousLevel);
            fileAppender.stop();
            encoder.stop();
        }

        String logContent = Files.readString(securityLogPath);
        assertTrue(logContent.contains("SEC_HTTP_ANOMALOUS_REQUEST"));
        assertTrue(logContent.contains("SEC_HTTP_ACCESS_DENIED"));
        assertTrue(logContent.contains("status=403"));
    }
}

