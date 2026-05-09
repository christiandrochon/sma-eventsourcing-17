package fr.cdrochon.smamonolithe.audit.compliance.api;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Entree historique d'un controle d'attente d'audit.
 */
public record AuditExpectationCheckEntry(
        Long id,
        String expectationCode,
        Instant checkedAt,
        String checkedBy,
        String status,
        Integer score,
        String scope,
        String findings,
        String remediationPlan,
        LocalDate dueDate,
        String evidenceUri,
        Integer crossGarageSampleSize,
        String insertedFrom
) {
}

