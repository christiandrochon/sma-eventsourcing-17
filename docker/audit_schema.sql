-- ============================================================
-- SCHEMA AUDIT RGPD
-- Base de données : audit (PostgreSQL)
-- Création manuelle si initdb_postgres.sh n'a pas été exécuté
-- ============================================================

-- Connexion à la base audit :
-- psql -U postgres -d audit

CREATE TABLE IF NOT EXISTS audit_events (
    id              BIGSERIAL                   PRIMARY KEY,
    event_time      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),

    -- QUI
    actor           VARCHAR(255)                NOT NULL,      -- utilisateur, service ou 'SYSTEM'
    actor_garage    VARCHAR(255),                              -- garage d'appartenance de l'acteur

    -- QUOI
    action          VARCHAR(100)                NOT NULL,      -- READ, CREATE, UPDATE, DELETE, ACCESS_DENIED, ANOMALY, LOGIN, LOGOUT
    resource        VARCHAR(100)                NOT NULL,      -- VEHICULE, CLIENT, GARAGE, DOSSIER, DOCUMENT, SESSION, UNKNOWN
    resource_id     VARCHAR(255),                              -- UUID ou identifiant de la ressource

    -- OÙ
    garage_id       VARCHAR(255),                              -- garage propriétaire de la ressource
    cross_garage    BOOLEAN                     NOT NULL DEFAULT FALSE,  -- acteur accède à une ressource d'un autre garage

    -- POURQUOI
    reason          TEXT,                                      -- justification libre (RGPD : doit être renseignée pour les accès cross-garage)

    -- RÉSULTAT
    result          VARCHAR(50)                 NOT NULL,      -- SUCCESS, DENIED, ERROR

    -- CONTEXTE HTTP
    http_method     VARCHAR(10),
    http_path       VARCHAR(1024),
    http_status     INTEGER,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(512),

    -- DONNÉES COMPLÉMENTAIRES
    details         TEXT                                       -- JSON ou texte libre
);

-- Index de recherche RGPD
CREATE INDEX IF NOT EXISTS idx_audit_actor       ON audit_events (actor);
CREATE INDEX IF NOT EXISTS idx_audit_event_time  ON audit_events (event_time);
CREATE INDEX IF NOT EXISTS idx_audit_resource    ON audit_events (resource, resource_id);
CREATE INDEX IF NOT EXISTS idx_audit_garage      ON audit_events (garage_id);
CREATE INDEX IF NOT EXISTS idx_audit_cross       ON audit_events (cross_garage) WHERE cross_garage = TRUE;

-- Sécurité RGPD : l'applicatif ne peut qu'insérer, jamais modifier ni supprimer
REVOKE UPDATE, DELETE ON audit_events FROM postgres;

-- ============================================================
-- EXEMPLES DE REQUÊTES RGPD
-- ============================================================

-- Qui a consulté quoi dans les 30 derniers jours ?
-- SELECT actor, action, resource, resource_id, event_time
-- FROM audit_events
-- WHERE event_time >= now() - INTERVAL '30 days'
-- ORDER BY event_time DESC;

-- Tous les accès cross-garage
-- SELECT * FROM audit_events WHERE cross_garage = TRUE ORDER BY event_time DESC;

-- Tous les accès refusés (401/403)
-- SELECT actor, http_path, http_status, ip_address, event_time
-- FROM audit_events
-- WHERE action = 'ACCESS_DENIED'
-- ORDER BY event_time DESC;

-- Historique complet d'un véhicule (portabilité / droit d'accès RGPD)
-- SELECT * FROM audit_events
-- WHERE resource = 'VEHICULE' AND resource_id = '<uuid>'
-- ORDER BY event_time;

