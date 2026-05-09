package fr.cdrochon.smamonolithe.audit.compliance.api;

import java.time.Instant;

/**
 * Dernier controle connu pour une attente d'audit.
 */
public record AuditExpectationLatestCheck(
        Long checkId,
        Instant checkedAt,
        String checkedBy,
        String status,
        Integer score,
        String findings,
        String remediationPlan,
        String evidenceUri
) {
}

