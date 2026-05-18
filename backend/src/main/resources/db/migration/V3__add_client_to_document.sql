-- =============================================================================
-- V3 – Ajout de la colonne client_id dans la table document
-- =============================================================================
-- Nécessaire pour le filtrage RBAC : un USER ne voit que ses propres documents.
-- La colonne est nullable (rétrocompatibilité avec les documents créés avant
-- cette migration, et avec les documents créés via l'application sans client_id).
-- =============================================================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'document'
          AND column_name = 'client_id'
    ) THEN
        ALTER TABLE document ADD COLUMN client_id VARCHAR(255);
    END IF;

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

CREATE INDEX IF NOT EXISTS idx_document_client ON document (client_id);

