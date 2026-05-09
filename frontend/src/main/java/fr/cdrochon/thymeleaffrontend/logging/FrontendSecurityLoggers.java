package fr.cdrochon.thymeleaffrontend.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loggers de sécurité du frontend.
 * <ul>
 *   <li>connexion / échec de communication avec le backend protégé</li>
 *   <li>accès refusé / usage anormal détecté</li>
 * </ul>
 */
public final class FrontendSecurityLoggers {

    private static final Logger UI_SECURITY = LoggerFactory.getLogger("UI_SECURITY");

    private FrontendSecurityLoggers() {
        // Utility class
    }

    public static Logger security() {
        return UI_SECURITY;
    }
}

