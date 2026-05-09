#!/bin/sh
set -e

echo "Init script: start"

: "${POSTGRES_USER:?POSTGRES_USER is required}"
: "${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is required}"
: "${POSTGRES_DB:?POSTGRES_DB is required}"

echo "Ensuring database '${POSTGRES_DB}' exists and granting privileges..."

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<EOSQL
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = '${POSTGRES_DB}') THEN
    EXECUTE format('CREATE DATABASE %I', '${POSTGRES_DB}');
  END IF;
END
\$\$;

-- le user POSTGRES_USER existe déjà (créé par l'image) si tu l'as fourni
GRANT ALL PRIVILEGES ON DATABASE "${POSTGRES_DB}" TO "${POSTGRES_USER}";
EOSQL

echo "Init script: monolithe database done"

# -----------------------------------------------------------------------
# BASE audit : traçabilité RGPD - non modifiable par l'applicatif
# -----------------------------------------------------------------------
echo "Creating 'audit' database..."

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<EOSQL
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'audit') THEN
    EXECUTE 'CREATE DATABASE audit';
  END IF;
END
\$\$;
GRANT ALL PRIVILEGES ON DATABASE audit TO "${POSTGRES_USER}";
EOSQL

echo "Creating audit schema and audit_events table..."

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname audit <<EOSQL
CREATE TABLE IF NOT EXISTS audit_events (
    id              BIGSERIAL                   PRIMARY KEY,
    event_time      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
    actor           VARCHAR(255)                NOT NULL,
    actor_garage    VARCHAR(255),
    action          VARCHAR(100)                NOT NULL,
    resource        VARCHAR(100)                NOT NULL,
    resource_id     VARCHAR(255),
    garage_id       VARCHAR(255),
    cross_garage    BOOLEAN                     NOT NULL DEFAULT FALSE,
    reason          TEXT,
    result          VARCHAR(50)                 NOT NULL,
    http_method     VARCHAR(10),
    http_path       VARCHAR(1024),
    http_status     INTEGER,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(512),
    details         TEXT
);

-- Grille d'attentes d'audit pour revue independante (RGPD + gouvernance data)
CREATE TABLE IF NOT EXISTS audit_expectations (
    code                  VARCHAR(40)                 PRIMARY KEY,
    domain                VARCHAR(100)                NOT NULL,
    title                 VARCHAR(255)                NOT NULL,
    description           TEXT                        NOT NULL,
    independent_evidence  TEXT                        NOT NULL,
    legal_reference       VARCHAR(255),
    expected_frequency    VARCHAR(50)                 NOT NULL DEFAULT 'MONTHLY',
    enabled               BOOLEAN                     NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now()
);

-- Historique des controles de conformite sur chaque attente
CREATE TABLE IF NOT EXISTS audit_expectation_checks (
    id                        BIGSERIAL                   PRIMARY KEY,
    expectation_code          VARCHAR(40)                 NOT NULL REFERENCES audit_expectations(code),
    checked_at                TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
    checked_by                VARCHAR(255)                NOT NULL,
    status                    VARCHAR(40)                 NOT NULL,
    score                     SMALLINT,
    scope                     VARCHAR(255),
    findings                  TEXT,
    remediation_plan          TEXT,
    due_date                  DATE,
    evidence_uri              VARCHAR(1024),
    cross_garage_sample_size  INTEGER,
    inserted_from             VARCHAR(100)                NOT NULL DEFAULT 'MANUAL'
);

-- Vue materialisant le dernier controle par attente
CREATE OR REPLACE VIEW audit_expectations_latest AS
SELECT
    e.code,
    e.domain,
    e.title,
    e.description,
    e.independent_evidence,
    e.legal_reference,
    e.expected_frequency,
    e.enabled,
    e.created_at,
    c.id AS check_id,
    c.checked_at,
    c.checked_by,
    c.status,
    c.score,
    c.scope,
    c.findings,
    c.remediation_plan,
    c.due_date,
    c.evidence_uri,
    c.cross_garage_sample_size,
    c.inserted_from
FROM audit_expectations e
LEFT JOIN LATERAL (
    SELECT cc.*
    FROM audit_expectation_checks cc
    WHERE cc.expectation_code = e.code
    ORDER BY cc.checked_at DESC
    LIMIT 1
) c ON TRUE;

-- Index pour recherche RGPD : qui a fait quoi, quand
CREATE INDEX IF NOT EXISTS idx_audit_actor       ON audit_events (actor);
CREATE INDEX IF NOT EXISTS idx_audit_event_time  ON audit_events (event_time);
CREATE INDEX IF NOT EXISTS idx_audit_resource    ON audit_events (resource, resource_id);
CREATE INDEX IF NOT EXISTS idx_audit_garage      ON audit_events (garage_id);
CREATE INDEX IF NOT EXISTS idx_audit_cross       ON audit_events (cross_garage) WHERE cross_garage = TRUE;

CREATE INDEX IF NOT EXISTS idx_audit_checks_code_time ON audit_expectation_checks (expectation_code, checked_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_checks_status    ON audit_expectation_checks (status);

-- Seed idempotent de la grille d'audit
INSERT INTO audit_expectations (code, domain, title, description, independent_evidence, legal_reference, expected_frequency)
VALUES
('GOV_001', 'GOVERNANCE', 'Roles and responsibilities are defined', 'The organization has named owners for privacy, security and data governance decisions.', 'Org chart, role letters, governance minutes.', 'RGPD Art. 5(2), Art. 24', 'QUARTERLY'),
('LAW_001', 'LEGAL_BASIS', 'Legal basis is documented', 'Each processing activity has a valid legal basis and explicit purpose.', 'RoPA extracts, legal memos, consent text snapshots.', 'RGPD Art. 6, Art. 13', 'QUARTERLY'),
('ROPA_001', 'ROPA', 'Processing register is maintained', 'Records of processing activities are up to date for all business units.', 'RoPA export with timestamps and owner signatures.', 'RGPD Art. 30', 'MONTHLY'),
('MIN_001', 'MINIMIZATION', 'Data minimization is enforced', 'Collected data is limited to what is necessary for each declared purpose.', 'Field inventory, retention matrix, controller approvals.', 'RGPD Art. 5(1)(c)', 'QUARTERLY'),
('RET_001', 'RETENTION', 'Retention and deletion are controlled', 'Retention periods are defined and purge jobs produce verifiable traces.', 'Purge execution logs, deletion tickets, SQL evidence.', 'RGPD Art. 5(1)(e), Art. 17', 'MONTHLY'),
('DSR_001', 'DATA_SUBJECT_RIGHTS', 'Data subject requests are handled on time', 'Access, rectification, erasure and portability requests are fulfilled within legal deadlines.', 'DSR ticket history, SLA dashboard, response templates.', 'RGPD Art. 12-23', 'MONTHLY'),
('IAM_001', 'ACCESS_CONTROL', 'Least privilege is applied', 'Access rights are granted on need-to-know basis and reviewed periodically.', 'Access review reports, IAM exports, approval workflows.', 'RGPD Art. 32', 'MONTHLY'),
('AUD_001', 'TRACEABILITY', 'Audit trail is complete and queryable', 'It is possible to prove who accessed what, when and why, including denied and suspicious attempts.', 'SQL extracts from audit_events and API snapshots.', 'RGPD Art. 5(2), Art. 30, Art. 32', 'WEEKLY'),
('XGR_001', 'CROSS_GARAGE', 'Cross-garage accesses are monitored', 'Cross-garage consultations are flagged, justified and periodically sampled.', 'cross_garage=true extracts, sampling reports, justifications.', 'RGPD Art. 5(1)(a), Art. 32', 'WEEKLY'),
('VND_001', 'PROCESSORS', 'Processor contracts are controlled', 'Sub-processors are bound by contracts and monitored for compliance.', 'DPA repository, vendor assessments, renewal logs.', 'RGPD Art. 28', 'QUARTERLY'),
('TRF_001', 'INTERNATIONAL_TRANSFER', 'International transfers are assessed', 'Transfers outside EEA are documented with safeguards and impact assessments.', 'SCC packages, TIA reports, transfer map.', 'RGPD Ch. V', 'QUARTERLY'),
('DPIA_001', 'DPIA', 'High-risk processing has DPIA', 'A DPIA exists and is maintained for processing with elevated risk.', 'DPIA documents, risk acceptance records, action plans.', 'RGPD Art. 35', 'QUARTERLY'),
('INC_001', 'INCIDENTS', 'Breach process is operational', 'Personal data breaches are detected, triaged and notified in legal time when required.', 'Incident timeline, decision logs, CNIL notification evidence.', 'RGPD Art. 33-34', 'MONTHLY'),
('RES_001', 'RESILIENCE', 'Backup and restore are tested', 'Critical data restoration tests are executed and documented.', 'Backup test reports, restore logs, RTO/RPO evidence.', 'RGPD Art. 32', 'MONTHLY'),
('QTY_001', 'DATA_QUALITY', 'Data quality controls exist', 'Controls ensure integrity, consistency and correction of inaccurate records.', 'Data quality KPIs, correction workflows, sampling outputs.', 'RGPD Art. 5(1)(d)', 'MONTHLY')
ON CONFLICT (code) DO NOTHING;

-- SECURITE RGPD : verrouiller les tables d'audit contre UPDATE/DELETE
REVOKE UPDATE, DELETE ON audit_events FROM "${POSTGRES_USER}";
REVOKE UPDATE, DELETE ON audit_expectations FROM "${POSTGRES_USER}";
REVOKE UPDATE, DELETE ON audit_expectation_checks FROM "${POSTGRES_USER}";

CREATE OR REPLACE FUNCTION prevent_audit_mutation() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Audit tables are append-only. % on % is forbidden.', TG_OP, TG_TABLE_NAME;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_audit_events_block_mutation ON audit_events;
CREATE TRIGGER trg_audit_events_block_mutation
BEFORE UPDATE OR DELETE ON audit_events
FOR EACH ROW EXECUTE FUNCTION prevent_audit_mutation();

DROP TRIGGER IF EXISTS trg_audit_expectation_checks_block_mutation ON audit_expectation_checks;
CREATE TRIGGER trg_audit_expectation_checks_block_mutation
BEFORE UPDATE OR DELETE ON audit_expectation_checks
FOR EACH ROW EXECUTE FUNCTION prevent_audit_mutation();
EOSQL

echo "Init script: audit database done"




##!/bin/bash
#set -e
#
## Wait for PostgreSQL to be ready
#until pg_isready -U "$POSTGRES_USER"; do
#  echo "Waiting for PostgreSQL to be ready..."
#  sleep 2
#done
#
##pour loger dans docker et executer le script =
##-> docker exec -it postgres-monolithe /bin/bash
##-> /docker-entrypoint-initdb.d/initdb_postgres.sh
##
##Connaitre le superuser
##-> SELECT usename FROM pg_user WHERE usesuper IS TRUE;
#
##Superutilisateur :
##-> psql -U postgres
##
##creer unsuperutilisateur (en etant connecté dejà comme superutilisateur)
##-> \du (verifier les roles existants et leurs attributs)
##-> CREATE ROLE postgres WITH SUPERUSER LOGIN;
##-> ALTER ROLE postgres WITH PASSWORD 'yourpassword'; (changer le mdp)
#
#echo "debut du script d'initialisation !!!!!"
#
## Vérifier que les variables d'environnement sont définies
#if [ -z "$POSTGRES_USER" ] || [ -z "$POSTGRES_PASSWORD" ]; then
#  echo "Les variables d'environnement POSTGRES_USER et POSTGRES_PASSWORD doivent être définies."
#  exit 1
#fi
## Check if the 'postgres' role exists
#if ! psql -U postgres -tc "SELECT 1 FROM pg_roles WHERE rolname = 'postgres'" | grep -q 1; then
#  echo "Creating 'postgres' role..."
#  psql -U postgres -c "CREATE ROLE postgres;"
#fi
#
## Check if environment variables are set
#if [ -z "$POSTGRES_USER" ] || [ -z "$POSTGRES_PASSWORD" ]; then
#  echo "Environment variables POSTGRES_USER and POSTGRES_PASSWORD must be set."
#  exit 1
#fi
#
## Retry logic for creating the PostgreSQL user
#for i in {1..5}; do
#  echo "Creating PostgreSQL user if it doesn't exist (attempt $i)..."
#  psql -U postgres -tc "SELECT 1 FROM pg_roles WHERE rolname = '${POSTGRES_USER}'" | grep -q 1 && break || \
#  psql -U postgres -c "CREATE USER ${POSTGRES_USER} WITH ENCRYPTED PASSWORD '${POSTGRES_PASSWORD}';" && break
#  sleep 2
#done
#
## Retry logic for creating the application database
#for i in {1..5}; do
#  echo "Creating application database if it doesn't exist (attempt $i)..."
#  psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = '${POSTGRES_DB}'" | grep -q 1 && break || \
#  psql -U postgres -c "CREATE DATABASE ${POSTGRES_DB} WITH OWNER ${POSTGRES_USER};" && break
#  sleep 2
#done
#
## Grant all privileges on the application database to the user
#echo "Granting privileges on application database..."
#psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE ${POSTGRES_DB} TO ${POSTGRES_USER};"
#
#echo "Script d'initialisation terminé."
#
###!/bin/bash
##set -e
##
### Wait for PostgreSQL to be ready
##until pg_isready -U "$POSTGRES_USER"; do
##  echo "Waiting for PostgreSQL to be ready..."
##  sleep 2
##done
##
#
### Create the user with a password for the application
##psql -U "$POSTGRES_USER" -c "CREATE USER ${POSTGRES_USER} WITH ENCRYPTED PASSWORD '${POSTGRES_PASSWORD}';"
### Create the database for the application if it doesn't exist
##psql -U "$POSTGRES_USER" -c "CREATE DATABASE ${POSTGRES_DB} WITH OWNER ${POSTGRES_USER};"
### Grant all privileges on the application database to the user
##psql -U "$POSTGRES_USER" -c "GRANT ALL PRIVILEGES ON DATABASE ${POSTGRES_DB} TO ${POSTGRES_USER};"
##
##
##
##
#
#
