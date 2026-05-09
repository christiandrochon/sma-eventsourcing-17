package fr.cdrochon.smamonolithe.audit.domain;

import java.time.Instant;

/**
 * Evénement d'audit RGPD.
 * Immuable : représente une entrée à insérer dans la table audit_events.
 * Une fois créé, cet objet ne peut pas être modifié (record Java).
 *
 * <pre>
 * Champs obligatoires : actor, action, resource, result
 * Champs optionnels  : resourceId, garageId, actorGarage, reason, httpMethod, httpPath,
 *                      httpStatus, ipAddress, userAgent, details
 * </pre>
 *
 * Utiliser le {@link AuditEventRecord.Builder} pour construire un événement.
 */
public record AuditEventRecord(

        /** Identifiant ou nom de l'acteur (utilisateur, service, SYSTEM). */
        String actor,

        /** Garage d'appartenance de l'acteur (pour détection cross-garage). */
        String actorGarage,

        /** Type d'action effectuée. */
        AuditAction action,

        /** Ressource métier concernée. */
        AuditResource resource,

        /** Identifiant de la ressource concernée (ex : UUID du véhicule). */
        String resourceId,

        /** Garage propriétaire de la ressource (peut différer de actorGarage). */
        String garageId,

        /** Accès cross-garage détecté : l'acteur accède à une ressource d'un autre garage. */
        boolean crossGarage,

        /** Justification de l'action (pourquoi – optionnel mais recommandé RGPD). */
        String reason,

        /** Résultat de l'action. */
        AuditResult result,

        /** Méthode HTTP de la requête (GET, POST, DELETE…). */
        String httpMethod,

        /** Chemin HTTP de la requête. */
        String httpPath,

        /** Code de statut HTTP de la réponse. */
        Integer httpStatus,

        /** Adresse IP de l'acteur. */
        String ipAddress,

        /** User-Agent du client. */
        String userAgent,

        /** Données complémentaires libres (JSON ou texte). */
        String details,

        /** Horodatage de l'événement (UTC). Calculé automatiquement si null. */
        Instant eventTime

) {

    // -----------------------------------------------------------------------
    // Builder fluent
    // -----------------------------------------------------------------------

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String actor = "SYSTEM";
        private String actorGarage;
        private AuditAction action;
        private AuditResource resource = AuditResource.UNKNOWN;
        private String resourceId;
        private String garageId;
        private boolean crossGarage = false;
        private String reason;
        private AuditResult result = AuditResult.SUCCESS;
        private String httpMethod;
        private String httpPath;
        private Integer httpStatus;
        private String ipAddress;
        private String userAgent;
        private String details;
        private Instant eventTime;

        private Builder() {}

        public Builder actor(String actor)               { this.actor = actor; return this; }
        public Builder actorGarage(String actorGarage)   { this.actorGarage = actorGarage; return this; }
        public Builder action(AuditAction action)         { this.action = action; return this; }
        public Builder resource(AuditResource resource)   { this.resource = resource; return this; }
        public Builder resourceId(String resourceId)      { this.resourceId = resourceId; return this; }
        public Builder garageId(String garageId)          { this.garageId = garageId; return this; }
        public Builder crossGarage(boolean crossGarage)   { this.crossGarage = crossGarage; return this; }
        public Builder reason(String reason)              { this.reason = reason; return this; }
        public Builder result(AuditResult result)         { this.result = result; return this; }
        public Builder httpMethod(String httpMethod)      { this.httpMethod = httpMethod; return this; }
        public Builder httpPath(String httpPath)          { this.httpPath = httpPath; return this; }
        public Builder httpStatus(Integer httpStatus)     { this.httpStatus = httpStatus; return this; }
        public Builder ipAddress(String ipAddress)        { this.ipAddress = ipAddress; return this; }
        public Builder userAgent(String userAgent)        { this.userAgent = userAgent; return this; }
        public Builder details(String details)            { this.details = details; return this; }
        public Builder eventTime(Instant eventTime)       { this.eventTime = eventTime; return this; }

        public AuditEventRecord build() {
            if (action == null) throw new IllegalStateException("AuditEventRecord: action is required");
            return new AuditEventRecord(
                    actor, actorGarage, action, resource, resourceId,
                    garageId, crossGarage, reason, result,
                    httpMethod, httpPath, httpStatus,
                    ipAddress, userAgent, details,
                    eventTime != null ? eventTime : Instant.now()
            );
        }
    }
}

