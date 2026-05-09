package fr.cdrochon.smamonolithe.audit.compliance.api;

/**
 * Vue synthetique pour un audit independant.
 */
public record AuditComplianceDashboard(
        long enabledExpectations,
        long expectationsWithAnyCheck,
        long compliantLatest,
        long partialLatest,
        long nonCompliantLatest,
        long notApplicableLatest,
        long accessDenied30d,
        long anomalies30d,
        long crossGarage30d
) {
}

