package fr.cdrochon.thymeleaffrontend.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loggers centralisés du frontend.
 * <ul>
 *   <li>{@link #access()} → navigation utilisateur, succès, actions (→ ui-access.log)</li>
 *   <li>{@link #error()}  → erreurs HTTP, exceptions, timeouts (→ ui-error.log)</li>
 * </ul>
 */
public final class FrontendLoggers {

    private static final Logger UI_ACCESS = LoggerFactory.getLogger("UI_ACCESS");
    private static final Logger UI_ERROR  = LoggerFactory.getLogger("UI_ERROR");

    private FrontendLoggers() {
        // Utility class
    }

    public static Logger access() {
        return UI_ACCESS;
    }

    public static Logger error() {
        return UI_ERROR;
    }
}

