package fr.cdrochon.smamonolithe.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loggers de sécurité du backend.
 * <ul>
 *   <li>tentatives d'accès non autorisées</li>
 *   <li>usage anormal / requêtes suspectes</li>
 * </ul>
 */
public final class BackendSecurityLoggers {

    private static final Logger SECURITY = LoggerFactory.getLogger("SECURITY");

    private BackendSecurityLoggers() {
        // Utility class
    }

    public static Logger security() {
        return SECURITY;
    }
}

