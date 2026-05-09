package fr.cdrochon.smamonolithe.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central logger for business events to route them to dedicated appenders.
 */
public final class BusinessLoggers {

    private static final Logger BUSINESS_LOG = LoggerFactory.getLogger("BUSINESS");

    private BusinessLoggers() {
        // Utility class
    }

    public static Logger business() {
        return BUSINESS_LOG;
    }
}

