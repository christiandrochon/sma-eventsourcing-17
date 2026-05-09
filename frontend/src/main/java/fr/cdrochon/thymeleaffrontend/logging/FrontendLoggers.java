package fr.cdrochon.thymeleaffrontend.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loggers centralisés du frontend.
 * <ul>
 *   <li>{@link #access()} → navigation utilisateur, succès, actions (→ logs/metier/ui-access.log)</li>
 *   <li>{@link #error()}  → erreurs HTTP, exceptions, timeouts (→ logs/technique/ui-error.log)</li>
 *   <li>{@link #tech()}   → traces techniques (latence, statut HTTP) (→ logs/technique/ui-technical.log)</li>
 *   <li>{@link #business()} → événements métier, opérations critiques (→ logs/metier/ui-business.log)</li>
 * </ul>
 */
public final class FrontendLoggers {

    private static final Logger UI_ACCESS = LoggerFactory.getLogger("UI_ACCESS");
    private static final Logger UI_ERROR  = LoggerFactory.getLogger("UI_ERROR");
    private static final Logger UI_TECH   = LoggerFactory.getLogger("UI_TECH");
    private static final Logger UI_BUSINESS = LoggerFactory.getLogger("UI_BUSINESS");

    private FrontendLoggers() {
        // Utility class
    }

    public static Logger access() {
        return UI_ACCESS;
    }

    public static Logger error() {
        return UI_ERROR;
    }

    public static Logger tech() {
        return UI_TECH;
    }

    public static Logger business() {
        return UI_BUSINESS;
    }
}

