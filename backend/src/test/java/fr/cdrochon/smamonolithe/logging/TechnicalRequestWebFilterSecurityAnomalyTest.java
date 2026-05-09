package fr.cdrochon.smamonolithe.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TechnicalRequestWebFilterSecurityAnomalyTest {

    private final Logger securityLogger = (Logger) LoggerFactory.getLogger("SECURITY");
    private final ListAppender<ILoggingEvent> securityAppender = new ListAppender<>();

    TechnicalRequestWebFilterSecurityAnomalyTest() {
        securityAppender.start();
        securityLogger.addAppender(securityAppender);
    }

    @AfterEach
    void cleanupAppenders() {
        securityAppender.list.clear();
    }

    @ParameterizedTest(name = "method={0}, path={1} -> suspiciousMethod={2}, suspiciousPath={3}")
    @CsvSource({
            "DELETE,/queries/vehicules,true,false",
            "GET,/queries/../admin,false,true",
            "GET,/queries//admin,false,true",
            "PATCH,/queries/../admin,true,true"
    })
    void shouldLogAnomalousRequestWhenMethodOrPathIsSuspicious(String method,
                                                               String path,
                                                               boolean expectedSuspiciousMethod,
                                                               boolean expectedSuspiciousPath) {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.method(HttpMethod.valueOf(method), path).build()
        );

        WebFilterChain chain = serverWebExchange -> {
            serverWebExchange.getResponse().setRawStatusCode(200);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        boolean anomalyLogExists = securityAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains("SEC_HTTP_ANOMALOUS_REQUEST")
                        && msg.contains("method=" + method)
                        && msg.contains("path=" + path)
                        && msg.contains("suspiciousMethod=" + expectedSuspiciousMethod)
                        && msg.contains("suspiciousPath=" + expectedSuspiciousPath));

        assertTrue(anomalyLogExists, "Le backend doit tracer SEC_HTTP_ANOMALOUS_REQUEST");
    }

    @Test
    void shouldNotLogAnomalousRequestForSafeMethodAndPath() {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/queries/vehicules").build()
        );

        WebFilterChain chain = serverWebExchange -> {
            serverWebExchange.getResponse().setRawStatusCode(200);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        boolean anomalyLogExists = securityAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains("SEC_HTTP_ANOMALOUS_REQUEST"));

        assertFalse(anomalyLogExists, "Le backend ne doit pas logger une requête normale comme anomalie");
    }
}

