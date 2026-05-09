package fr.cdrochon.smamonolithe.audit.domain;

/**
 * Ressources métier traçables dans le journal d'audit RGPD.
 */
public enum AuditResource {

    /** Véhicule automobile. */
    VEHICULE,

    /** Client (propriétaire d'un véhicule). */
    CLIENT,

    /** Garage (structure de maintenance). */
    GARAGE,

    /** Dossier de réparation. */
    DOSSIER,

    /** Document lié à un dossier. */
    DOCUMENT,

    /** Session utilisateur (login/logout). */
    SESSION,

    /** Ressource non catégorisée ou inconnue. */
    UNKNOWN
}

