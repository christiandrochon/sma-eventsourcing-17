-- =============================================================================
-- V1 – Création du schéma de l'application SMA (Event Sourcing / CQRS)
-- =============================================================================
-- Utilisation de IF NOT EXISTS pour rendre le script idempotent
-- (base existante créée par Hibernate ddl-auto=update → pas d'erreur au 1er run)
--
-- Convention de nommage : SpringPhysicalNamingStrategy de Hibernate
--   camelCase → snake_case  (ex: nomClient → nom_client)
--
-- Encodage des énumérations :
--   @Enumerated (sans paramètre) → ORDINAL → INTEGER / SMALLINT
--   @Enumerated(EnumType.STRING) → VARCHAR(255)
--
-- Correspondance énumérations ORDINAL (ordre déclaration dans le code Java) :
--   ClientStatus      : ACTIF=0, HISTORISE=1, INACTIF=2
--   DossierStatus     : OUVERT=0, CLOTURE=1, REOUVERT=2, ANNULE=3, REFUSE=4,
--                       MODIFIE=5, VALIDE=6, REJET=7, ACCEPTE=8, TRAITE=9,
--                       ENVOYE=10, RECU=11, RETOURNE=12, ARCHIVE=13,
--                       DESARCHIVE=14, SUPPRIME=15, RESTAURE=16, PURGE=17
--   GarageStatus      : CREATED=0, DELETED=1
--   DocumentStatusDTO : CREATED=0, SENT=1, PAID=2, CANCELLED=3, REFUSED=4,
--                       ACCEPTED=5, IN_PROGRESS=6, COMPLETED=7, ARCHIVED=8,
--                       DELETED=9, DRAFT=10, VALIDATED=11, TO_BE_VALIDATED=12
--   Pays              : AFGHANISTAN=0 … FRANCE=61, BELGIQUE=16 …
-- =============================================================================

-- ---------------------------------------------------------------------------
-- TABLE : vehicule
-- Créée en premier car référencée par client et dossier
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vehicule (
    id_vehicule                          VARCHAR(255)  NOT NULL,
    immatriculation_vehicule             VARCHAR(255),
    date_mise_en_circulation_vehicule    TIMESTAMP,
    vehicule_status                      VARCHAR(255),    -- @Enumerated(EnumType.STRING)
    client_id                            VARCHAR(255),    -- FK → client (ajoutée après)

    CONSTRAINT pk_vehicule PRIMARY KEY (id_vehicule)
);

-- ---------------------------------------------------------------------------
-- TABLE : client
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS client (
    id                  VARCHAR(255)  NOT NULL,
    nom_client          VARCHAR(255),
    prenom_client       VARCHAR(255),
    mail_client         VARCHAR(255),
    tel_client          VARCHAR(255),
    -- AdresseClient (embeddable)
    numero_de_rue       VARCHAR(255),
    rue                 VARCHAR(255),
    complement_adresse  VARCHAR(255),
    cp                  VARCHAR(255),
    ville               VARCHAR(255),
    pays                INTEGER,                          -- @Enumerated ORDINAL
    client_status       INTEGER,                         -- @Enumerated ORDINAL
    vehicule_id         VARCHAR(255),                    -- FK → vehicule

    CONSTRAINT pk_client PRIMARY KEY (id)
);

-- ---------------------------------------------------------------------------
-- TABLE : dossier
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS dossier (
    id                          VARCHAR(255)  NOT NULL,
    nom_dossier                 VARCHAR(255),
    date_creation_dossier       TIMESTAMP,
    date_modification_dossier   TIMESTAMP,
    client_id                   VARCHAR(255),            -- FK → client
    vehicule_id                 VARCHAR(255),            -- FK → vehicule
    dossier_status              INTEGER,                 -- @Enumerated ORDINAL

    CONSTRAINT pk_dossier PRIMARY KEY (id)
);

-- ---------------------------------------------------------------------------
-- TABLE : document
-- (TypeDocument est @Embeddable, ses champs sont inclus dans la table)
-- Les colonnes date_* ont été déclarées avec columnDefinition TIMESTAMP WITHOUT TIME ZONE
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS document (
    id                          VARCHAR(255)  NOT NULL,
    nom_document                VARCHAR(255),
    titre_document              VARCHAR(255),
    emetteur_du_document        VARCHAR(255),
    nom_type_document           VARCHAR(255),            -- from embedded TypeDocument
    date_creation_document      TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    date_modification_document  TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    document_status             INTEGER,                 -- @Enumerated ORDINAL
    client_id                   VARCHAR(255),            -- FK -> client.id

    CONSTRAINT pk_document PRIMARY KEY (id)
);

-- ---------------------------------------------------------------------------
-- TABLE : garage
-- (AdresseGarage est @Embeddable – ses champs sont inclus directement)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS garage (
    id_query            VARCHAR(255)  NOT NULL,          -- @Id idQuery → id_query
    nom_garage          VARCHAR(255),
    mail_responsable    VARCHAR(255),
    -- AdresseGarage (embeddable)
    numero_de_rue       VARCHAR(255),
    rue                 VARCHAR(255),
    cp                  VARCHAR(255),
    ville               VARCHAR(255),
    garage_status       INTEGER,                         -- @Enumerated ORDINAL

    CONSTRAINT pk_garage PRIMARY KEY (id_query)
);

-- ---------------------------------------------------------------------------
-- TABLE : garage_transaction
-- garageQuery est @ManyToOne sans @JoinColumn → Hibernate génère :
--   fieldName_pkColumnName = garageQuery_idQuery → garage_query_id_query
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS garage_transaction (
    id                      BIGSERIAL     NOT NULL,      -- @GeneratedValue(IDENTITY)
    instant                 TIMESTAMP,
    transaction_type        VARCHAR(255),                -- @Enumerated(EnumType.STRING)
    garage_query_id_query   VARCHAR(255),                -- FK → garage.id_query

    CONSTRAINT pk_garage_transaction PRIMARY KEY (id)
);

-- =============================================================================
-- FOREIGN KEYS (ajoutées après la création de toutes les tables)
-- =============================================================================

-- client.vehicule_id → vehicule.id_vehicule
ALTER TABLE client
    ADD CONSTRAINT fk_client_vehicule
    FOREIGN KEY (vehicule_id) REFERENCES vehicule(id_vehicule);

-- vehicule.client_id → client.id
ALTER TABLE vehicule
    ADD CONSTRAINT fk_vehicule_client
    FOREIGN KEY (client_id) REFERENCES client(id);

-- dossier.client_id → client.id
ALTER TABLE dossier
    ADD CONSTRAINT fk_dossier_client
    FOREIGN KEY (client_id) REFERENCES client(id);

-- dossier.vehicule_id → vehicule.id_vehicule
ALTER TABLE dossier
    ADD CONSTRAINT fk_dossier_vehicule
    FOREIGN KEY (vehicule_id) REFERENCES vehicule(id_vehicule);

-- garage_transaction.garage_query_id_query → garage.id_query
ALTER TABLE garage_transaction
    ADD CONSTRAINT fk_garagetx_garage
    FOREIGN KEY (garage_query_id_query) REFERENCES garage(id_query);

-- document.client_id -> client.id
ALTER TABLE document
    ADD COLUMN IF NOT EXISTS client_id VARCHAR(255);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_document_client'
    ) THEN
        ALTER TABLE document
            ADD CONSTRAINT fk_document_client
            FOREIGN KEY (client_id) REFERENCES client(id);
    END IF;
END
$$;

-- =============================================================================
-- INDEX utiles (recherches fréquentes)
-- =============================================================================
CREATE INDEX IF NOT EXISTS idx_client_mail       ON client(mail_client);
CREATE INDEX IF NOT EXISTS idx_client_nom        ON client(nom_client, prenom_client);
CREATE INDEX IF NOT EXISTS idx_vehicule_immat    ON vehicule(immatriculation_vehicule);
CREATE INDEX IF NOT EXISTS idx_dossier_status    ON dossier(dossier_status);
CREATE INDEX IF NOT EXISTS idx_document_status   ON document(document_status);
CREATE INDEX IF NOT EXISTS idx_document_type     ON document(nom_type_document);
CREATE INDEX IF NOT EXISTS idx_document_client   ON document(client_id);
CREATE INDEX IF NOT EXISTS idx_garage_nom        ON garage(nom_garage);

