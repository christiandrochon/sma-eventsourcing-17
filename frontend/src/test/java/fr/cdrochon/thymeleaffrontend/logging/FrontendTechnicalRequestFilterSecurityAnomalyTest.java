package fr.cdrochon.thymeleaffrontend.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontendTechnicalRequestFilterSecurityAnomalyTest {

    private final Logger securityLogger = (Logger) LoggerFactory.getLogger("UI_SECURITY");
    private final ListAppender<ILoggingEvent> securityAppender = new ListAppender<>();

    FrontendTechnicalRequestFilterSecurityAnomalyTest() {
        securityAppender.start();
        securityLogger.addAppender(securityAppender);
    }

    @AfterEach
    void cleanupAppenders() {
        securityAppender.list.clear();
    }

    @ParameterizedTest(name = "method={0}, path={1} -> suspiciousMethod={2}, suspiciousPath={3}")
    @CsvSource({
            "DELETE,/ui/vehicules,true,false",
            "GET,/ui/../admin,false,true",
            "GET,/ui//admin,false,true",
            "PUT,/ui/../admin,true,true"
    })
    void shouldLogAnomalousRequestWhenMethodOrPathIsSuspicious(String method,
                                                               String path,
                                                               boolean expectedSuspiciousMethod,
                                                               boolean expectedSuspiciousPath) throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> ((MockHttpServletResponse) res).setStatus(200);

        filter.doFilterInternal(request, response, chain);

        boolean anomalyLogExists = securityAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains("SEC_FRONTEND_ANOMALOUS_REQUEST")
                        && msg.contains("method=" + method)
                        && msg.contains("path=" + path)
                        && msg.contains("suspiciousMethod=" + expectedSuspiciousMethod)
                        && msg.contains("suspiciousPath=" + expectedSuspiciousPath));

        assertTrue(anomalyLogExists, "Le frontend doit tracer SEC_FRONTEND_ANOMALOUS_REQUEST");
    }

    @Test
    void shouldNotLogAnomalousRequestForSafeMethodAndPath() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ui/vehicules");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> ((MockHttpServletResponse) res).setStatus(200);

        filter.doFilterInternal(request, response, chain);

        boolean anomalyLogExists = securityAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(msg -> msg.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"));

        assertFalse(anomalyLogExists, "Le frontend ne doit pas logger une requête normale comme anomalie");
    }
}

