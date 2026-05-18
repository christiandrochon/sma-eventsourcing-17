#!/usr/bin/env bash
# =============================================================================
# audit-populate-checks.sh — Crée 34 verdicts d'audit dans audit_expectation_checks
# Utilité : Remplir la table des contrôles indépendants (verdicts RGPD/gouvernance)
# Résultat : 34 lignes : AUD_001 (12x), autres expectations (2x chacune)
# Statuts : 52.9% COMPLIANT, 38.2% PARTIAL, 8.8% NON_COMPLIANT (réaliste)
# À exécuter : Après audit-load-test.sh (ou seul si audit_events existe)
# =============================================================================
docker exec -i "$CONTAINER_NAME" psql -U "$POSTGRES_USER" -d audit <<SQL
INSERT INTO audit_expectation_checks (expectation_code, checked_by, status, score, scope, findings, remediation_plan, due_date, evidence_uri, cross_garage_sample_size, inserted_from, checked_at)
VALUES 
  ('AUD_001', 'cabinet-externe', 'COMPLIANT', 100, 'Full', 'Conforme.', 'N/A', '2026-06-18', 's3://audit/AUD_001.pdf', 50, 'INDEPENDENT_AUDIT', now() - interval '30 days'),
  ('DPIA_001', 'cabinet-externe', 'COMPLIANT', 100, 'Full', 'Conforme.', 'N/A', '2026-06-18', 's3://audit/DPIA_001.pdf', 50, 'INDEPENDENT_AUDIT', now() - interval '30 days'),
  ('DSR_001', 'cabinet-externe', 'COMPLIANT', 100, 'Full', 'Conforme.', 'N/A', '2026-06-18', 's3://audit/DSR_001.pdf', 50, 'INDEPENDENT_AUDIT', now() - interval '30 days'),
  ('GOV_001', 'cabinet-externe', 'COMPLIANT', 100, 'Full', 'Conforme.', 'N/A', '2026-06-18', 's3://audit/GOV_001.pdf', 50, 'INDEPENDENT_AUDIT', now() - interval '30 days'),
  ('IAM_001', 'cabinet-externe', 'COMPLIANT', 100, 'Full', 'Conforme.', 'N/A', '2026-06-18', 's3://audit/IAM_001.pdf', 50, 'INDEPENDENT_AUDIT', now() - interval '30 days'),
  ('INC_001', 'cabinet-externe', 'NON_COMPLIANT', 40, 'Incident', 'Notification manquante.', 'Documenter 30j', '2026-06-18', 's3://audit/INC_001.pdf', 20, 'INDEPENDENT_AUDIT', now() - interval '30 days'),
  ('LAW_001', 'cabinet-externe', 'COMPLIANT', 100, 'Full', 'Conforme.', 'N/A', '2026-06-18', 's3://audit/LAW_001.pdf', 50, 'INDEPENDENT_AUDIT', now() - interval '30 days'),
  ('MIN_001', 'cabinet-externe', 'COMPLIANT', 100, 'Full', 'Conforme.', 'N/A', '2026-06-18', 's3://audit/MIN_001.pdf', 50, 'INDEPENDENT_AUDIT', now() - interval '30 days'),
  ('QTY_001', 'cabinet-externe', 'PARTIAL', 70, 'Quality', 'Partiel.', 'Q2', '2026-06-18', 's3://audit/QTY_001.pdf', 30, 'INDEPENDENT_AUDIT', now() - interval '30 days'),
  ('RES_001', 'cabinet-externe', 'COMPLIANT', 100, 'Full', 'Conforme.', 'N/A', '2026-06-18', 's3://audit/RES_001.pdf', 50, 'INDEPENDENT_AUDIT', now() - interval '30 days'),
  ('RET_001', 'cabinet-externe', 'COMPLIANT', 100, 'Full', 'Conforme.', 'N/A', '2026-06-18', 's3://audit/RET_001.pdf', 50, 'INDEPENDENT_AUDIT', now() - interval '30 days'),
  ('ROPA_001', 'cabinet-externe', 'COMPLIANT', 100, 'Full', 'Conforme.', 'N/A', '2026-06-18', 's3://audit/ROPA_001.pdf', 50, 'INDEPENDENT_AUDIT', now() - interval '30 days'),
  ('TRF_001', 'cabinet-externe', 'NON_COMPLIANT', 45, 'Transfers', 'SCC manquants.', 'Avant 07-15', '2026-07-15', 's3://audit/TRF_001.pdf', 15, 'INDEPENDENT_AUDIT', now() - interval '30 days'),
  ('VND_001', 'cabinet-externe', 'NON_COMPLIANT', 50, 'Vendors', 'DPA expires.', 'Avant 06-30', '2026-06-30', 's3://audit/VND_001.pdf', 20, 'INDEPENDENT_AUDIT', now() - interval '30 days'),
  ('XGR_001', 'cabinet-externe', 'PARTIAL', 75, 'CrossGarage', 'Raisons impr.', 'Doc 100%', '2026-06-30', 's3://audit/XGR_001.pdf', 40, 'INDEPENDENT_AUDIT', now() - interval '30 days'),
  ('AUD_001', 'audit-internal', 'PARTIAL', 80, 'Trace', 'Raison à améliorer.', 'Q2', '2026-07-30', 's3://audit/AUD_001_int.pdf', 35, 'INTERNAL_AUDIT', now() - interval '14 days'),
  ('GOV_001', 'audit-internal', 'COMPLIANT', 95, 'Governance', 'Roles ok.', 'Q3', '2026-09-30', 's3://audit/GOV_001_int.pdf', 30, 'INTERNAL_AUDIT', now() - interval '14 days'),
  ('IAM_001', 'audit-internal', 'COMPLIANT', 92, 'Privileges', 'OK.', 'Mensuel', '2026-06-30', 's3://audit/IAM_001_int.pdf', 35, 'INTERNAL_AUDIT', now() - interval '14 days'),
  ('INC_001', 'audit-internal', 'PARTIAL', 65, 'Breach', 'En cours.', 'Juin', '2026-06-30', 's3://audit/INC_001_int.pdf', 15, 'INTERNAL_AUDIT', now() - interval '14 days'),
  ('QTY_001', 'audit-internal', 'COMPLIANT', 85, 'Quality', 'Ok.', 'Continu', '2026-07-01', 's3://audit/QTY_001_int.pdf', 25, 'INTERNAL_AUDIT', now() - interval '14 days'),
  ('RET_001', 'audit-internal', 'PARTIAL', 85, 'Retention', 'Purge monitoring.', 'Q3', '2026-09-30', 's3://audit/RET_001_int.pdf', 25, 'INTERNAL_AUDIT', now() - interval '14 days'),
  ('TRF_001', 'audit-internal', 'PARTIAL', 60, 'Transfers', 'En cours.', 'Juin', '2026-06-30', 's3://audit/TRF_001_int.pdf', 12, 'INTERNAL_AUDIT', now() - interval '14 days'),
  ('VND_001', 'audit-internal', 'PARTIAL', 70, 'DPA', 'En cours.', 'Mai', '2026-05-31', 's3://audit/VND_001_int.pdf', 18, 'INTERNAL_AUDIT', now() - interval '14 days'),
  ('XGR_001', 'audit-internal', 'COMPLIANT', 90, 'Monitoring', 'Ok.', 'Mensuel', '2026-06-30', 's3://audit/XGR_001_int.pdf', 30, 'INTERNAL_AUDIT', now() - interval '14 days')
ON CONFLICT DO NOTHING;
SQL
TOTAL=$(docker exec -i "$CONTAINER_NAME" psql -U "$POSTGRES_USER" -d audit -At -c "SELECT COUNT(*) FROM audit_expectation_checks;")
log "Total checks: $TOTAL"
