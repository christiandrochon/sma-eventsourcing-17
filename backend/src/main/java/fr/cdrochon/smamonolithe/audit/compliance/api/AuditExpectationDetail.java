package fr.cdrochon.smamonolithe.audit.compliance.api;

import java.util.List;

/**
 * Vue detaillee d'une attente d'audit avec son historique de controles.
 */
public record AuditExpectationDetail(
        AuditExpectationItem expectation,
        List<AuditExpectationCheckEntry> history
) {
}

