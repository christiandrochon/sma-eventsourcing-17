package fr.cdrochon.smamonolithe.audit.domain;

/**
 * Résultat d'une action auditée.
 */
public enum AuditResult {

    /** L'action a réussi. */
    SUCCESS,

    /** L'accès a été refusé (non autorisé). */
    DENIED,

    /** Une erreur technique s'est produite. */
    ERROR
}

