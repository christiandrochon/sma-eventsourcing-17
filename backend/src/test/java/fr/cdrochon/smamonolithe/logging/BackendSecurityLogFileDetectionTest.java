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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de politique des fichiers de log de sécurité backend.
 * Couvre les cas d'erreur : méthodes suspectes, chemins suspects, accès refusés (401/403),
 * et vérifie l'absence de traces dans le log sécurité pour les requêtes normales.
 */
class BackendSecurityLogFileDetectionTest {

    @TempDir
    Path tempDir;

    private static final AtomicInteger counter = new AtomicInteger(0);

    // ──────────────────────────────────────────────────────────────────────────
    // Méthode utilitaire : capture du log SECURITY pour un scénario donné
    // ──────────────────────────────────────────────────────────────────────────

    private String captureSecurityLog(ThrowingRunnable action) throws Exception {
        Path logFile = tempDir.resolve("security-" + counter.incrementAndGet() + ".log");

        Logger securityLogger = (Logger) LoggerFactory.getLogger("SECURITY");
        LoggerContext context = securityLogger.getLoggerContext();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%msg%n");
        encoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setName("TEST_SEC_BACKEND_" + counter.get());
        fileAppender.setFile(logFile.toString());
        fileAppender.setEncoder(encoder);
        fileAppender.setAppend(false);
        fileAppender.start();

        Level previousLevel = securityLogger.getLevel();
        securityLogger.setLevel(Level.WARN);
        securityLogger.addAppender(fileAppender);

        try {
            action.run();
        } finally {
            securityLogger.detachAppender(fileAppender);
            securityLogger.setLevel(previousLevel);
            fileAppender.stop();
            encoder.stop();
        }

        return logFile.toFile().exists() ? Files.readString(logFile) : "";
    }

    private void runExchange(TechnicalRequestWebFilter filter, HttpMethod method, String path, int status) {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.method(method, path).build()
        );
        WebFilterChain chain = ex -> {
            ex.getResponse().setStatusCode(HttpStatusCode.valueOf(status));
            return Mono.empty();
        };
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GROUPE 1 – Méthodes HTTP suspectes → SEC_HTTP_ANOMALOUS_REQUEST
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void delete_method_shouldTrigger_SEC_HTTP_ANOMALOUS_REQUEST() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.DELETE, "/queries/vehicules", 200));
        assertTrue(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"), "DELETE doit déclencher ANOMALOUS");
    }

    @Test
    void put_method_shouldTrigger_SEC_HTTP_ANOMALOUS_REQUEST() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.PUT, "/queries/ressource", 200));
        assertTrue(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"), "PUT doit déclencher ANOMALOUS");
    }

    @Test
    void patch_method_shouldTrigger_SEC_HTTP_ANOMALOUS_REQUEST() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.PATCH, "/queries/ressource", 200));
        assertTrue(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"), "PATCH doit déclencher ANOMALOUS");
    }

    @Test
    void trace_method_shouldTrigger_SEC_HTTP_ANOMALOUS_REQUEST() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.TRACE, "/queries/ressource", 200));
        assertTrue(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"), "TRACE doit déclencher ANOMALOUS");
    }

    @Test
    void anomalous_log_shouldContain_method_DELETE() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.DELETE, "/queries/vehicules", 200));
        assertTrue(log.contains("method=DELETE"), "Le log doit contenir method=DELETE");
    }

    @Test
    void anomalous_log_shouldContain_suspiciousMethod_true() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.DELETE, "/queries/vehicules", 200));
        assertTrue(log.contains("suspiciousMethod=true"), "Le log doit indiquer suspiciousMethod=true");
    }

    @Test
    void anomalous_log_shouldContain_the_correct_path() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.DELETE, "/queries/vehicules", 200));
        assertTrue(log.contains("path=/queries/vehicules"), "Le log doit contenir le chemin exact");
    }

    @Test
    void anomalous_log_shouldContain_traceId() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.DELETE, "/queries/vehicules", 200));
        assertTrue(log.contains("traceId="), "Le log ANOMALOUS doit contenir le traceId");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GROUPE 2 – Méthodes autorisées → PAS de SEC_HTTP_ANOMALOUS_REQUEST
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void get_method_shouldNot_trigger_SEC_HTTP_ANOMALOUS_REQUEST() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/vehicules", 200));
        assertFalse(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"), "GET est autorisé côté backend");
    }

    @Test
    void post_method_shouldNot_trigger_SEC_HTTP_ANOMALOUS_REQUEST() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.POST, "/commands/create", 200));
        assertFalse(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"), "POST est autorisé côté backend");
    }

    @Test
    void head_method_shouldNot_trigger_SEC_HTTP_ANOMALOUS_REQUEST() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.HEAD, "/queries/health", 200));
        assertFalse(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"), "HEAD est autorisé côté backend");
    }

    @Test
    void options_method_shouldNot_trigger_SEC_HTTP_ANOMALOUS_REQUEST() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        // OPTIONS est autorisé côté backend (contrairement au frontend)
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.OPTIONS, "/queries/vehicules", 200));
        assertFalse(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"), "OPTIONS est autorisé côté backend");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GROUPE 3 – Chemins suspects → SEC_HTTP_ANOMALOUS_REQUEST
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void path_with_dotdot_shouldTrigger_SEC_HTTP_ANOMALOUS_REQUEST() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/../admin", 200));
        assertTrue(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"), "Chemin avec .. doit être suspect");
    }

    @Test
    void path_with_doubleSlash_isNormalizedByFramework_soNot_SEC_HTTP_ANOMALOUS_REQUEST() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        // MockServerHttpRequest normalise /queries//admin → /queries/admin : le filtre ne voit jamais //
        // Ce test documente cette limitation du stack WebFlux/Reactor
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries//admin", 200));
        assertFalse(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"),
                "MockServerHttpRequest normalise // : le filtre ne peut pas détecter ce cas");
    }

    @Test
    void path_with_encoded_2e_shouldTrigger_SEC_HTTP_ANOMALOUS_REQUEST() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/%2eadmin", 200));
        assertTrue(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"), "Chemin avec %2e doit être suspect");
    }

    @Test
    void path_with_encoded_2f_shouldTrigger_SEC_HTTP_ANOMALOUS_REQUEST() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/%2fadmin", 200));
        assertTrue(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"), "Chemin avec %2f doit être suspect");
    }

    @Test
    void path_with_backslash_shouldTrigger_SEC_HTTP_ANOMALOUS_REQUEST() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/admin\\secret", 200));
        assertTrue(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"), "Chemin avec \\ doit être suspect");
    }

    @Test
    void path_with_dotdot_log_shouldContain_suspiciousPath_true() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/../etc/passwd", 200));
        assertTrue(log.contains("suspiciousPath=true"), "Le log doit indiquer suspiciousPath=true");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GROUPE 4 – Accès refusés 401 / 403 → SEC_HTTP_ACCESS_DENIED
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void get_returning_401_shouldTrigger_SEC_HTTP_ACCESS_DENIED() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/secure", 401));
        assertTrue(log.contains("SEC_HTTP_ACCESS_DENIED"), "401 doit générer SEC_HTTP_ACCESS_DENIED");
    }

    @Test
    void get_returning_403_shouldTrigger_SEC_HTTP_ACCESS_DENIED() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/admin", 403));
        assertTrue(log.contains("SEC_HTTP_ACCESS_DENIED"), "403 doit générer SEC_HTTP_ACCESS_DENIED");
    }

    @Test
    void post_returning_401_shouldTrigger_SEC_HTTP_ACCESS_DENIED() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.POST, "/commands/action", 401));
        assertTrue(log.contains("SEC_HTTP_ACCESS_DENIED"), "POST 401 doit générer SEC_HTTP_ACCESS_DENIED");
    }

    @Test
    void post_returning_403_shouldTrigger_SEC_HTTP_ACCESS_DENIED() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.POST, "/commands/action", 403));
        assertTrue(log.contains("SEC_HTTP_ACCESS_DENIED"), "POST 403 doit générer SEC_HTTP_ACCESS_DENIED");
    }

    @Test
    void accessDenied_log_shouldContain_status_401() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/secure", 401));
        assertTrue(log.contains("status=401"), "Le log doit contenir status=401");
    }

    @Test
    void accessDenied_log_shouldContain_status_403() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/admin", 403));
        assertTrue(log.contains("status=403"), "Le log doit contenir status=403");
    }

    @Test
    void accessDenied_log_shouldContain_path() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/private", 403));
        assertTrue(log.contains("path=/queries/private"), "Le log ACCESS_DENIED doit contenir le chemin exact");
    }

    @Test
    void accessDenied_log_shouldContain_traceId() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/secure", 403));
        assertTrue(log.contains("traceId="), "Le log ACCESS_DENIED doit contenir traceId");
    }

    @Test
    void accessDenied_log_shouldContain_durationMs() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/secure", 401));
        assertTrue(log.contains("durationMs="), "Le log ACCESS_DENIED doit contenir durationMs");
    }

    @Test
    void delete_returning_403_shouldTrigger_both_anomalous_and_accessDenied() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.DELETE, "/queries/vehicules", 403));
        assertTrue(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"), "DELETE doit déclencher ANOMALOUS");
        assertTrue(log.contains("SEC_HTTP_ACCESS_DENIED"), "403 doit déclencher ACCESS_DENIED");
    }

    @Test
    void put_returning_401_shouldTrigger_both_anomalous_and_accessDenied() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.PUT, "/commands/ressource", 401));
        assertTrue(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"), "PUT doit déclencher ANOMALOUS");
        assertTrue(log.contains("SEC_HTTP_ACCESS_DENIED"), "401 doit déclencher ACCESS_DENIED");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GROUPE 5 – Requêtes normales → AUCUNE trace dans le log sécurité
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void get_200_shouldNot_appear_in_security_log() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/vehicules", 200));
        assertFalse(log.contains("SEC_HTTP"), "GET 200 ne doit générer aucun log sécurité");
    }

    @Test
    void post_200_shouldNot_appear_in_security_log() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.POST, "/commands/create", 200));
        assertFalse(log.contains("SEC_HTTP"), "POST 200 ne doit générer aucun log sécurité");
    }

    @Test
    void get_302_shouldNot_appear_in_security_log() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/redirect", 302));
        assertFalse(log.contains("SEC_HTTP"), "302 ne doit pas être loggué en sécurité");
    }

    @Test
    void get_404_shouldNot_appear_in_security_log() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/not-found", 404));
        assertFalse(log.contains("SEC_HTTP"), "404 ne doit pas être loggué en sécurité");
    }

    @Test
    void get_500_shouldNot_appear_in_security_log() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/error", 500));
        assertFalse(log.contains("SEC_HTTP"), "500 ne doit pas être loggué en sécurité");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GROUPE 6 – Cas limites (encodage majuscule, requêtes mixtes)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void path_with_uppercase_2E_shouldNot_trigger_anomalous() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/%2Eadmin", 200));
        assertFalse(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"),
                "%2E en majuscules ne doit pas déclencher ANOMALOUS (case-sensitive)");
    }

    @Test
    void path_with_uppercase_2F_shouldNot_trigger_anomalous() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> runExchange(filter, HttpMethod.GET, "/queries/%2Fadmin", 200));
        assertFalse(log.contains("SEC_HTTP_ANOMALOUS_REQUEST"),
                "%2F en majuscules ne doit pas déclencher ANOMALOUS (case-sensitive)");
    }

    @Test
    void multiple_anomalous_requests_shouldAllAppearInLog() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> {
            runExchange(filter, HttpMethod.DELETE, "/queries/a", 200);
            runExchange(filter, HttpMethod.PUT, "/queries/b", 200);
            runExchange(filter, HttpMethod.PATCH, "/queries/c", 200);
        });
        assertTrue(log.contains("method=DELETE"), "DELETE doit apparaître dans le log");
        assertTrue(log.contains("method=PUT"), "PUT doit apparaître dans le log");
        assertTrue(log.contains("method=PATCH"), "PATCH doit apparaître dans le log");
    }

    @Test
    void multiple_access_denied_shouldAllAppearInLog() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> {
            runExchange(filter, HttpMethod.GET, "/queries/resource-a", 401);
            runExchange(filter, HttpMethod.GET, "/queries/resource-b", 403);
        });
        assertTrue(log.contains("path=/queries/resource-a"), "resource-a doit être loggué");
        assertTrue(log.contains("path=/queries/resource-b"), "resource-b doit être loggué");
    }

    @Test
    void normal_get_followed_by_anomalous_shouldLog_only_anomalous() throws Exception {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter();
        String log = captureSecurityLog(() -> {
            runExchange(filter, HttpMethod.GET, "/queries/ok", 200);
            runExchange(filter, HttpMethod.DELETE, "/queries/danger", 200);
        });
        assertFalse(log.contains("path=/queries/ok"), "La requête normale ne doit pas apparaître en sécurité");
        assertTrue(log.contains("path=/queries/danger"), "La requête suspecte doit apparaître");
    }
}
