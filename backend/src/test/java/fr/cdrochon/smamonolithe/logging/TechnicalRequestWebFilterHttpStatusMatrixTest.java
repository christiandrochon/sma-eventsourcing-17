package fr.cdrochon.smamonolithe.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cette classe contient uniquement des tests unitaires.
 */
class TechnicalRequestWebFilterHttpStatusMatrixTest {

    private final Logger technicalLogger = (Logger) LoggerFactory.getLogger(TechnicalRequestWebFilter.class);
    private final Logger securityLogger = (Logger) LoggerFactory.getLogger("SECURITY");

    private final ListAppender<ILoggingEvent> technicalAppender = new ListAppender<>();
    private final ListAppender<ILoggingEvent> securityAppender = new ListAppender<>();

    TechnicalRequestWebFilterHttpStatusMatrixTest() {
        technicalAppender.start();
        securityAppender.start();
        technicalLogger.addAppender(technicalAppender);
        securityLogger.addAppender(securityAppender);
    }

    @AfterEach
    void cleanupAppenders() {
        technicalAppender.list.clear();
        securityAppender.list.clear();
    }

    @ParameterizedTest(name = "backend status={0}")
    @MethodSource("hundredStatuses")
    void shouldTraceHttpStatusesFrom2xxTo5xx(int status) {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter(NoopAuditServiceFactory.noop());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/queries/status/" + status).build()
        );

        WebFilterChain chain = serverWebExchange -> {
            serverWebExchange.getResponse().setStatusCode(HttpStatusCode.valueOf(status));
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        boolean technicalLineExists = technicalAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains("TECH_HTTP") && msg.contains("status=" + status));
        assertTrue(technicalLineExists, "Le log technique backend doit contenir le status " + status);

        boolean accessDeniedSecurityLog = securityAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains("SEC_HTTP_ACCESS_DENIED") && msg.contains("status=" + status));

        if (status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value()) {
            assertTrue(accessDeniedSecurityLog, "Un status 401/403 doit alimenter SECURITY côté backend");
        } else {
            assertFalse(accessDeniedSecurityLog, "Seuls 401/403 doivent générer ACCESS_DENIED côté backend");
        }
    }

    static IntStream hundredStatuses() {
        List<Integer> statuses = new ArrayList<>(100);

        // 2xx (27)
        IntStream.rangeClosed(200, 226).forEach(statuses::add);

        // 3xx (9)
        statuses.add(300);
        statuses.add(301);
        statuses.add(302);
        statuses.add(303);
        statuses.add(304);
        statuses.add(305);
        statuses.add(307);
        statuses.add(308);
        statuses.add(310);

        // 4xx (52)
        IntStream.rangeClosed(400, 451).forEach(statuses::add);

        // 5xx (12)
        IntStream.rangeClosed(500, 511).forEach(statuses::add);

        if (statuses.size() != 100) {
            throw new IllegalStateException("La matrice backend doit contenir exactement 100 statuts, trouvé=" + statuses.size());
        }

        return statuses.stream().mapToInt(Integer::intValue);
    }
}

