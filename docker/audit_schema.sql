-- ============================================================
-- SCHEMA AUDIT RGPD + GRILLE D'AUDIT INDEPENDANT
-- Base de donnees : audit (PostgreSQL)
-- Creation manuelle si initdb_postgres.sh n'a pas ete execute
-- ============================================================

-- Connexion a la base audit :
-- psql -U postgres -d audit

-- ------------------------------------------------------------------
-- 1) JOURNAL D'EVENEMENTS (qui a consulte quoi / quand / pourquoi)
-- ------------------------------------------------------------------
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

CREATE INDEX IF NOT EXISTS idx_audit_actor       ON audit_events (actor);
CREATE INDEX IF NOT EXISTS idx_audit_event_time  ON audit_events (event_time);
CREATE INDEX IF NOT EXISTS idx_audit_resource    ON audit_events (resource, resource_id);
CREATE INDEX IF NOT EXISTS idx_audit_garage      ON audit_events (garage_id);
CREATE INDEX IF NOT EXISTS idx_audit_cross       ON audit_events (cross_garage) WHERE cross_garage = TRUE;

-- ------------------------------------------------------------------
-- 2) GRILLE D'ATTENTES D'AUDIT (ce qu'un auditeur independant verifie)
-- ------------------------------------------------------------------
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

CREATE INDEX IF NOT EXISTS idx_audit_checks_code_time ON audit_expectation_checks (expectation_code, checked_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_checks_status    ON audit_expectation_checks (status);

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

-- ------------------------------------------------------------------
-- 3) SEED DE LA GRILLE (idempotent)
-- ------------------------------------------------------------------
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

-- ------------------------------------------------------------------
-- 4) APPEND-ONLY (interdiction de modifier/supprimer les traces)
-- ------------------------------------------------------------------
REVOKE UPDATE, DELETE ON audit_events FROM postgres;
REVOKE UPDATE, DELETE ON audit_expectations FROM postgres;
REVOKE UPDATE, DELETE ON audit_expectation_checks FROM postgres;

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

-- ============================================================
-- EXEMPLES DE CONSULTATION
-- ============================================================

-- Grille complete avec dernier etat connu
-- SELECT code, domain, title, status, checked_at
-- FROM audit_expectations_latest
-- ORDER BY domain, code;

-- Qui a consulte quoi (30 jours)
-- SELECT actor, action, resource, resource_id, event_time
-- FROM audit_events
-- WHERE event_time >= now() - INTERVAL '30 days'
-- ORDER BY event_time DESC;

-- Tous les acces cross-garage
-- SELECT * FROM audit_events WHERE cross_garage = TRUE ORDER BY event_time DESC;

-- Historique des controles d'une attente
-- SELECT * FROM audit_expectation_checks
-- WHERE expectation_code = 'AUD_001'
-- ORDER BY checked_at DESC;
