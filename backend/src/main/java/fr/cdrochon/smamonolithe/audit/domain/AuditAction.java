package fr.cdrochon.smamonolithe.audit.domain;

/**
 * Actions traçables dans le journal d'audit RGPD.
 * Chaque action correspond à une interaction significative sur une ressource métier.
 */
public enum AuditAction {

    /** Consultation d'une ressource (lecture simple). */
    READ,

    /** Création d'une nouvelle ressource. */
    CREATE,

    /** Modification d'une ressource existante. */
    UPDATE,

    /** Suppression d'une ressource. */
    DELETE,

    /** Tentative d'accès refusée (401 / 403). */
    ACCESS_DENIED,

    /** Requête suspecte détectée (méthode ou chemin anormal). */
    ANOMALY,

    /** Connexion (ouverture de session). */
    LOGIN,

    /** Déconnexion (fermeture de session). */
    LOGOUT
}

