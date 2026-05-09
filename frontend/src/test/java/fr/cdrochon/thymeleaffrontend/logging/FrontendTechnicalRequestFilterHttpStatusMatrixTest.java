package fr.cdrochon.thymeleaffrontend.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontendTechnicalRequestFilterHttpStatusMatrixTest {

    private final Logger accessLogger = (Logger) LoggerFactory.getLogger("UI_ACCESS");
    private final Logger techLogger = (Logger) LoggerFactory.getLogger("UI_TECH");
    private final Logger securityLogger = (Logger) LoggerFactory.getLogger("UI_SECURITY");

    private final ListAppender<ILoggingEvent> accessAppender = new ListAppender<>();
    private final ListAppender<ILoggingEvent> techAppender = new ListAppender<>();
    private final ListAppender<ILoggingEvent> securityAppender = new ListAppender<>();

    FrontendTechnicalRequestFilterHttpStatusMatrixTest() {
        accessAppender.start();
        techAppender.start();
        securityAppender.start();

        accessLogger.addAppender(accessAppender);
        techLogger.addAppender(techAppender);
        securityLogger.addAppender(securityAppender);
    }

    @AfterEach
    void cleanupAppenders() {
        accessAppender.list.clear();
        techAppender.list.clear();
        securityAppender.list.clear();
    }

    @ParameterizedTest(name = "frontend status={0}")
    @MethodSource("hundredStatuses")
    void shouldTraceHttpStatusesFrom2xxTo5xx(int status) throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ui/status/" + status);
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> ((MockHttpServletResponse) res).setStatus(status);

        filter.doFilterInternal(request, response, chain);

        boolean accessLineExists = accessAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains("UI_ACCESS_HTTP") && msg.contains("status=" + status));
        assertTrue(accessLineExists, "Le log d'accès frontend doit contenir le status " + status);

        boolean technicalLineExists = techAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains("UI_TECH_HTTP") && msg.contains("status=" + status));
        assertTrue(technicalLineExists, "Le log technique frontend doit contenir le status " + status);

        boolean accessDeniedSecurityLog = securityAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains("SEC_FRONTEND_ACCESS_DENIED") && msg.contains("status=" + status));

        if (status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value()) {
            assertTrue(accessDeniedSecurityLog, "Un status 401/403 doit alimenter UI_SECURITY côté frontend");
        } else {
            assertFalse(accessDeniedSecurityLog, "Seuls 401/403 doivent générer ACCESS_DENIED côté frontend");
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
            throw new IllegalStateException("La matrice frontend doit contenir exactement 100 statuts, trouvé=" + statuses.size());
        }

        return statuses.stream().mapToInt(Integer::intValue);
    }
}

