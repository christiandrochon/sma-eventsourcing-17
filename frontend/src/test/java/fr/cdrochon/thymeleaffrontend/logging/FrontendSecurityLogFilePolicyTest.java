package fr.cdrochon.thymeleaffrontend.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de politique des fichiers de log de sécurité frontend.
 * Couvre les cas d'erreur : méthodes suspectes, chemins suspects, accès refusés (401/403),
 * et vérifie l'absence de traces dans le log sécurité pour les requêtes normales.
 */
class FrontendSecurityLogFilePolicyTest {

    @TempDir
    Path tempDir;

    // ──────────────────────────────────────────────────────────────────────────
    // Méthode utilitaire : capture du contenu du log UI_SECURITY pour un scénario
    // ──────────────────────────────────────────────────────────────────────────

    private static final AtomicInteger counter = new AtomicInteger(0);

    /**
     * Renvoi les traces 
     * @param action
     * @return
     * @throws Exception
     */
    private String captureSecurityLog(ThrowingRunnable action) throws Exception {
        Path logFile = tempDir.resolve("ui-security-" + counter.incrementAndGet() + ".log");

        Logger securityLogger = (Logger) LoggerFactory.getLogger("UI_SECURITY");
        LoggerContext context = securityLogger.getLoggerContext();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%msg%n");
        encoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setName("TEST_UI_SEC_" + counter.get());
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

    private void runRequest(FrontendTechnicalRequestFilter filter, String method, String path, int status) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((MockHttpServletResponse) res).setStatus(status);
        filter.doFilterInternal(request, response, chain);
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GROUPE 1 – Méthodes HTTP suspectes → SEC_FRONTEND_ANOMALOUS_REQUEST
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void delete_method_shouldTrigger_SEC_FRONTEND_ANOMALOUS_REQUEST() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "DELETE", "/ui/ressource", 200));
        assertTrue(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "DELETE doit être détecté comme suspect");
    }

    @Test
    void put_method_shouldTrigger_SEC_FRONTEND_ANOMALOUS_REQUEST() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "PUT", "/ui/ressource", 200));
        assertTrue(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "PUT doit être détecté comme suspect");
    }

    @Test
    void patch_method_shouldTrigger_SEC_FRONTEND_ANOMALOUS_REQUEST() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "PATCH", "/ui/ressource", 200));
        assertTrue(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "PATCH doit être détecté comme suspect");
    }

    @Test
    void trace_method_shouldTrigger_SEC_FRONTEND_ANOMALOUS_REQUEST() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "TRACE", "/ui/ressource", 200));
        assertTrue(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "TRACE doit être détecté comme suspect");
    }

    @Test
    void options_method_shouldTrigger_SEC_FRONTEND_ANOMALOUS_REQUEST() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "OPTIONS", "/ui/ressource", 200));
        assertTrue(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "OPTIONS doit être détecté comme suspect côté frontend");
    }

    @Test
    void connect_method_shouldTrigger_SEC_FRONTEND_ANOMALOUS_REQUEST() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "CONNECT", "/ui/ressource", 200));
        assertTrue(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "CONNECT doit être détecté comme suspect");
    }

    @Test
    void delete_log_shouldContain_method_DELETE() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "DELETE", "/ui/vehicules", 200));
        assertTrue(log.contains("method=DELETE"), "Le log doit contenir method=DELETE");
    }

    @Test
    void delete_log_shouldContain_suspiciousMethod_true() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "DELETE", "/ui/vehicules", 200));
        assertTrue(log.contains("suspiciousMethod=true"), "Le log doit indiquer suspiciousMethod=true");
    }

    @Test
    void delete_log_shouldContain_correct_path() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "DELETE", "/ui/vehicules", 200));
        assertTrue(log.contains("path=/ui/vehicules"), "Le log doit contenir le chemin exact");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GROUPE 2 – Chemins suspects → SEC_FRONTEND_ANOMALOUS_REQUEST
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void path_with_dotdot_shouldTrigger_SEC_FRONTEND_ANOMALOUS_REQUEST() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/../admin", 200));
        assertTrue(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "Chemin avec .. doit être suspect");
    }

    @Test
    void path_with_doubleSlash_shouldTrigger_SEC_FRONTEND_ANOMALOUS_REQUEST() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui//admin", 200));
        assertTrue(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "Chemin avec // doit être suspect");
    }

    @Test
    void path_with_encoded_2e_shouldTrigger_SEC_FRONTEND_ANOMALOUS_REQUEST() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/%2eadmin", 200));
        assertTrue(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "Chemin avec %2e doit être suspect");
    }

    @Test
    void path_with_encoded_2f_shouldTrigger_SEC_FRONTEND_ANOMALOUS_REQUEST() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/%2fadmin", 200));
        assertTrue(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "Chemin avec %2f doit être suspect");
    }

    @Test
    void path_with_backslash_shouldTrigger_SEC_FRONTEND_ANOMALOUS_REQUEST() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/admin\\secret", 200));
        assertTrue(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "Chemin avec \\ doit être suspect");
    }

    @Test
    void path_with_dotdot_log_shouldContain_suspiciousPath_true() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/../etc/passwd", 200));
        assertTrue(log.contains("suspiciousPath=true"), "Le log doit indiquer suspiciousPath=true");
    }

    @Test
    void path_with_dotdot_in_middle_shouldTrigger_SEC_FRONTEND_ANOMALOUS_REQUEST() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/ok/../secret", 200));
        assertTrue(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "Chemin traversal en milieu doit être suspect");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GROUPE 3 – Accès refusés 401 / 403 → SEC_FRONTEND_ACCESS_DENIED
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void get_returning_401_shouldTrigger_SEC_FRONTEND_ACCESS_DENIED() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/secure", 401));
        assertTrue(log.contains("SEC_FRONTEND_ACCESS_DENIED"), "401 doit générer SEC_FRONTEND_ACCESS_DENIED");
    }

    @Test
    void get_returning_403_shouldTrigger_SEC_FRONTEND_ACCESS_DENIED() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/admin", 403));
        assertTrue(log.contains("SEC_FRONTEND_ACCESS_DENIED"), "403 doit générer SEC_FRONTEND_ACCESS_DENIED");
    }

    @Test
    void post_returning_401_shouldTrigger_SEC_FRONTEND_ACCESS_DENIED() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "POST", "/ui/login", 401));
        assertTrue(log.contains("SEC_FRONTEND_ACCESS_DENIED"), "POST 401 doit générer SEC_FRONTEND_ACCESS_DENIED");
    }

    @Test
    void post_returning_403_shouldTrigger_SEC_FRONTEND_ACCESS_DENIED() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "POST", "/ui/action", 403));
        assertTrue(log.contains("SEC_FRONTEND_ACCESS_DENIED"), "POST 403 doit générer SEC_FRONTEND_ACCESS_DENIED");
    }

    @Test
    void accessDenied_log_shouldContain_status_401() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/secure", 401));
        assertTrue(log.contains("status=401"), "Le log doit contenir status=401");
    }

    @Test
    void accessDenied_log_shouldContain_status_403() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/admin", 403));
        assertTrue(log.contains("status=403"), "Le log doit contenir status=403");
    }

    @Test
    void accessDenied_log_shouldContain_path() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/private", 403));
        assertTrue(log.contains("path=/ui/private"), "Le log ACCESS_DENIED doit contenir le chemin");
    }

    @Test
    void accessDenied_log_shouldContain_durationMs() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/secure", 401));
        assertTrue(log.contains("durationMs="), "Le log ACCESS_DENIED doit contenir durationMs");
    }

    @Test
    void delete_returning_403_shouldTrigger_both_anomalous_and_accessDenied() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "DELETE", "/ui/vehicules", 403));
        assertTrue(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "DELETE doit déclencher ANOMALOUS");
        assertTrue(log.contains("SEC_FRONTEND_ACCESS_DENIED"), "403 doit déclencher ACCESS_DENIED");
    }

    @Test
    void put_returning_401_shouldTrigger_both_anomalous_and_accessDenied() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "PUT", "/ui/ressource", 401));
        assertTrue(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "PUT doit déclencher ANOMALOUS");
        assertTrue(log.contains("SEC_FRONTEND_ACCESS_DENIED"), "401 doit déclencher ACCESS_DENIED");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GROUPE 4 – Requêtes normales → AUCUNE trace dans le log sécurité
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void get_200_shouldNot_appear_in_security_log() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/home", 200));
        assertFalse(log.contains("SEC_FRONTEND"), "GET 200 ne doit générer aucun log sécurité");
    }

    @Test
    void post_200_shouldNot_appear_in_security_log() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "POST", "/ui/submit", 200));
        assertFalse(log.contains("SEC_FRONTEND"), "POST 200 ne doit générer aucun log sécurité");
    }

    @Test
    void head_200_shouldNot_appear_in_security_log() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "HEAD", "/ui/ping", 200));
        assertFalse(log.contains("SEC_FRONTEND"), "HEAD 200 ne doit générer aucun log sécurité");
    }

    @Test
    void get_302_redirect_shouldNot_appear_in_security_log() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/redirect", 302));
        assertFalse(log.contains("SEC_FRONTEND"), "302 ne doit pas être loggué en sécurité");
    }

    @Test
    void get_404_shouldNot_appear_in_security_log() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/not-found", 404));
        assertFalse(log.contains("SEC_FRONTEND"), "404 ne doit pas être loggué en sécurité");
    }

    @Test
    void get_429_shouldNot_appear_in_security_log() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/too-many", 429));
        assertFalse(log.contains("SEC_FRONTEND"), "429 ne doit pas être loggué en sécurité");
    }

    @Test
    void get_500_shouldNot_appear_in_security_log() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/server-error", 500));
        assertFalse(log.contains("SEC_FRONTEND"), "500 serveur ne doit pas déclencher un log sécurité");
    }

    @Test
    void get_503_shouldNot_appear_in_security_log() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/unavailable", 503));
        assertFalse(log.contains("SEC_FRONTEND"), "503 ne doit pas être loggué en sécurité");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // GROUPE 5 – Cas limites (encodage majuscule, chemins propres)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void path_with_uppercase_2E_shouldNot_trigger_anomalous() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        // Le filtre vérifie %2e en minuscule uniquement → %2E ne doit pas être détecté
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/%2Eadmin", 200));
        assertFalse(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"),
                "%2E en majuscules ne doit pas déclencher ANOMALOUS (vérification case-sensitive)");
    }

    @Test
    void path_with_uppercase_2F_shouldNot_trigger_anomalous() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/%2Fadmin", 200));
        assertFalse(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"),
                "%2F en majuscules ne doit pas déclencher ANOMALOUS (vérification case-sensitive)");
    }

    @Test
    void head_method_shouldNot_trigger_anomalous() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "HEAD", "/ui/health", 200));
        assertFalse(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "HEAD est une méthode autorisée");
    }

    @Test
    void get_method_shouldNot_trigger_anomalous() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "GET", "/ui/clients", 200));
        assertFalse(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "GET est une méthode autorisée");
    }

    @Test
    void post_method_shouldNot_trigger_anomalous() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> runRequest(filter, "POST", "/ui/clients/create", 201));
        assertFalse(log.contains("SEC_FRONTEND_ANOMALOUS_REQUEST"), "POST est une méthode autorisée");
    }

    @Test
    void multiple_anomalous_requests_shouldAllAppearInLog() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> {
            runRequest(filter, "DELETE", "/ui/a", 200);
            runRequest(filter, "PUT", "/ui/b", 200);
            runRequest(filter, "PATCH", "/ui/c", 200);
        });
        assertTrue(log.contains("method=DELETE"), "DELETE doit apparaître dans le log");
        assertTrue(log.contains("method=PUT"), "PUT doit apparaître dans le log");
        assertTrue(log.contains("method=PATCH"), "PATCH doit apparaître dans le log");
    }

    @Test
    void multiple_access_denied_shouldAllAppearInLog() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> {
            runRequest(filter, "GET", "/ui/resource-a", 401);
            runRequest(filter, "GET", "/ui/resource-b", 403);
        });
        assertTrue(log.contains("path=/ui/resource-a"), "Le chemin resource-a doit être loggué");
        assertTrue(log.contains("path=/ui/resource-b"), "Le chemin resource-b doit être loggué");
    }

    @Test
    void normal_get_followed_by_anomalous_shouldLog_only_anomalous() throws Exception {
        FrontendTechnicalRequestFilter filter = new FrontendTechnicalRequestFilter();
        String log = captureSecurityLog(() -> {
            runRequest(filter, "GET", "/ui/ok", 200);
            runRequest(filter, "DELETE", "/ui/danger", 200);
        });
        assertFalse(log.contains("path=/ui/ok"), "La requête normale ne doit pas apparaître en sécurité");
        assertTrue(log.contains("path=/ui/danger"), "La requête suspecte doit apparaître");
    }
}
