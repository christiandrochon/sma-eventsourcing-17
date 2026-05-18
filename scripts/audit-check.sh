#!/usr/bin/env bash
# =============================================================================
# audit-check.sh — Diagnostic complet de la base d'audit (santé, schéma, données)
# Utilité : Vérifier rapidement que l'audit DB est présente et remplie
# Vérifications : backend health, DB audit existe, tables existent, seed appliqué, derniers événements
# Options : --apply-schema pour créer automatiquement le schéma s'il manque
# Résultat : Affiche PASS/WARN/FAIL pour chaque vérification
# À exécuter : Anytime pour diagnostiquer l'état d'audit
# =============================================================================
set -u
set -o pipefail


CONTAINER_NAME="${AUDIT_PG_CONTAINER:-postgres-monolithe}"
BACKEND_URL="${AUDIT_BACKEND_URL:-http://localhost:8092}"
POSTGRES_USER="${AUDIT_POSTGRES_USER:-postgres}"
WORKDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SCHEMA_FILE="$WORKDIR/docker/audit_schema.sql"

PASS=0
WARN=0
FAIL=0

log() { printf '%s\n' "$*"; }
pass() { PASS=$((PASS + 1)); log "[PASS] $*"; }
warn() { WARN=$((WARN + 1)); log "[WARN] $*"; }
fail() { FAIL=$((FAIL + 1)); log "[FAIL] $*"; }

have_cmd() {
  command -v "$1" >/dev/null 2>&1
}

run_psql() {
  local db="$1"
  local sql="$2"
  docker exec -i "$CONTAINER_NAME" psql -U "$POSTGRES_USER" -d "$db" -At -c "$sql" 2>/dev/null
}

apply_schema_if_requested() {
  if [[ "${1:-}" != "--apply-schema" ]]; then
    return 0
  fi

  if [[ ! -f "$SCHEMA_FILE" ]]; then
    fail "Schema file not found: $SCHEMA_FILE"
    return 1
  fi

  log "[INFO] Applying schema from docker/audit_schema.sql ..."
  docker exec -i "$CONTAINER_NAME" bash -lc "psql -U '$POSTGRES_USER' -d postgres -tc \"SELECT 1 FROM pg_database WHERE datname='audit'\" | grep -q 1 || psql -U '$POSTGRES_USER' -d postgres -c \"CREATE DATABASE audit\"" >/dev/null 2>&1
  if docker exec -i "$CONTAINER_NAME" psql -U "$POSTGRES_USER" -d audit < "$SCHEMA_FILE" >/dev/null 2>&1; then
    pass "Schema applied successfully"
  else
    fail "Schema application failed"
  fi
}

main() {
  local mode="${1:-}"

  log "=== SMA Audit Check ==="
  log "Container: $CONTAINER_NAME"
  log "Backend:   $BACKEND_URL"

  if ! have_cmd docker; then
    fail "docker command not found"
    log "Summary: PASS=$PASS WARN=$WARN FAIL=$FAIL"
    exit 2
  fi

  if ! have_cmd curl; then
    fail "curl command not found"
    log "Summary: PASS=$PASS WARN=$WARN FAIL=$FAIL"
    exit 2
  fi

  if docker ps --format '{{.Names}}' | grep -qx "$CONTAINER_NAME"; then
    pass "PostgreSQL container is running"
  else
    fail "PostgreSQL container '$CONTAINER_NAME' is not running"
    log "Summary: PASS=$PASS WARN=$WARN FAIL=$FAIL"
    exit 2
  fi

  apply_schema_if_requested "$mode"

  if curl -fsS "$BACKEND_URL/actuator/health" >/dev/null 2>&1; then
    pass "Backend health endpoint is reachable"
  else
    warn "Backend health endpoint is not reachable"
  fi

  local has_audit_db
  has_audit_db="$(run_psql postgres "SELECT 1 FROM pg_database WHERE datname='audit' LIMIT 1;")"
  if [[ "$has_audit_db" == "1" ]]; then
    pass "Database 'audit' exists"
  else
    fail "Database 'audit' does not exist"
    log "Hint: run with --apply-schema"
    log "Summary: PASS=$PASS WARN=$WARN FAIL=$FAIL"
    exit 2
  fi

  local has_expectations has_checks has_events
  has_expectations="$(run_psql audit "SELECT to_regclass('public.audit_expectations') IS NOT NULL;")"
  has_checks="$(run_psql audit "SELECT to_regclass('public.audit_expectation_checks') IS NOT NULL;")"
  has_events="$(run_psql audit "SELECT to_regclass('public.audit_events') IS NOT NULL;")"

  [[ "$has_expectations" == "t" ]] && pass "Table audit_expectations exists" || fail "Table audit_expectations missing"
  [[ "$has_checks" == "t" ]] && pass "Table audit_expectation_checks exists" || fail "Table audit_expectation_checks missing"
  [[ "$has_events" == "t" ]] && pass "Table audit_events exists" || fail "Table audit_events missing"

  local expectations_count
  expectations_count="$(run_psql audit "SELECT COUNT(*) FROM audit_expectations;")"
  if [[ -n "$expectations_count" && "$expectations_count" -ge 15 ]]; then
    pass "Expectations seed detected ($expectations_count rows)"
  else
    warn "Expectations seed seems low ($expectations_count rows)"
  fi

  local latest_checks
  latest_checks="$(run_psql audit "SELECT COUNT(*) FROM audit_expectation_checks;")"
  if [[ -n "$latest_checks" && "$latest_checks" -gt 0 ]]; then
    pass "At least one independent check is recorded ($latest_checks)"
  else
    warn "No independent check recorded yet in audit_expectation_checks"
  fi

  local latest_events
  latest_events="$(run_psql audit "SELECT COUNT(*) FROM audit_events WHERE event_time >= now() - interval '30 days';")"
  if [[ -n "$latest_events" && "$latest_events" -gt 0 ]]; then
    pass "Operational evidence exists in audit_events over last 30 days ($latest_events)"
  else
    warn "No recent audit events over last 30 days"
  fi

  local has_trigger_events has_trigger_checks
  has_trigger_events="$(run_psql audit "SELECT COUNT(*) FROM pg_trigger WHERE tgname='trg_audit_events_block_mutation';")"
  has_trigger_checks="$(run_psql audit "SELECT COUNT(*) FROM pg_trigger WHERE tgname='trg_audit_expectation_checks_block_mutation';")"

  [[ "$has_trigger_events" == "1" ]] && pass "Append-only trigger exists for audit_events" || warn "Missing append-only trigger on audit_events"
  [[ "$has_trigger_checks" == "1" ]] && pass "Append-only trigger exists for audit_expectation_checks" || warn "Missing append-only trigger on audit_expectation_checks"

  log "Summary: PASS=$PASS WARN=$WARN FAIL=$FAIL"
  if [[ "$FAIL" -gt 0 ]]; then
    exit 1
  fi
}

main "$@"

