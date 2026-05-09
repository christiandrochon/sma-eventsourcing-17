package fr.cdrochon.smamonolithe.audit.compliance.api;

import java.time.Instant;

/**
 * Definition d'une attente d'audit et son etat le plus recent.
 */
public record AuditExpectationItem(
        String code,
        String domain,
        String title,
        String description,
        String independentEvidence,
        String legalReference,
        String expectedFrequency,
        boolean enabled,
        Instant createdAt,
        AuditExpectationLatestCheck latestCheck
) {
}

