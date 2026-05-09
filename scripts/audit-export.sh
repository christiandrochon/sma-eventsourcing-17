#!/usr/bin/env bash
set -u
set -o pipefail

# Export helper for independent audit evidence.
# Generates CSV files from audit database:
# - audit_expectations_latest
# - audit_events over N days
# - cross-garage events over N days
# - audit_expectation_checks history

CONTAINER_NAME="${AUDIT_PG_CONTAINER:-postgres-monolithe}"
POSTGRES_USER="${AUDIT_POSTGRES_USER:-postgres}"
EXPORT_DAYS="${AUDIT_EXPORT_DAYS:-30}"
OUTPUT_BASE="${AUDIT_EXPORT_DIR:-./audit-exports}"

PASS=0
FAIL=0

log() { printf '%s\n' "$*"; }
pass() { PASS=$((PASS + 1)); log "[PASS] $*"; }
fail() { FAIL=$((FAIL + 1)); log "[FAIL] $*"; }

usage() {
  cat <<'EOF'
Usage:
  ./scripts/audit-export.sh [--days N] [--output-dir DIR]

Options:
  --days N          Number of days for rolling-window exports (default: 30)
  --output-dir DIR  Output folder root (default: ./audit-exports)

Environment variables:
  AUDIT_PG_CONTAINER   PostgreSQL container name (default: postgres-monolithe)
  AUDIT_POSTGRES_USER  PostgreSQL user (default: postgres)
  AUDIT_EXPORT_DAYS    Default rolling-window size (default: 30)
  AUDIT_EXPORT_DIR     Default output root (default: ./audit-exports)
EOF
}

parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --days)
        EXPORT_DAYS="$2"
        shift 2
        ;;
      --output-dir)
        OUTPUT_BASE="$2"
        shift 2
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        log "Unknown option: $1"
        usage
        exit 2
        ;;
    esac
  done
}

ensure_prereqs() {
  if ! command -v docker >/dev/null 2>&1; then
    fail "docker command not found"
    return 1
  fi

  if ! [[ "$EXPORT_DAYS" =~ ^[0-9]+$ ]]; then
    fail "--days must be a positive integer"
    return 1
  fi

  if ! docker ps --format '{{.Names}}' | grep -qx "$CONTAINER_NAME"; then
    fail "PostgreSQL container '$CONTAINER_NAME' is not running"
    return 1
  fi

  local has_audit_db
  has_audit_db="$(docker exec -i "$CONTAINER_NAME" psql -U "$POSTGRES_USER" -d postgres -At -c "SELECT 1 FROM pg_database WHERE datname='audit' LIMIT 1;" 2>/dev/null)"
  if [[ "$has_audit_db" != "1" ]]; then
    fail "Database 'audit' not found"
    return 1
  fi

  return 0
}

export_csv() {
  local outfile="$1"
  local sql="$2"

  if docker exec -i "$CONTAINER_NAME" psql -U "$POSTGRES_USER" -d audit -c "\\copy ($sql) TO STDOUT WITH CSV HEADER" > "$outfile" 2>/dev/null; then
    pass "Exported $(basename "$outfile")"
  else
    fail "Failed to export $(basename "$outfile")"
  fi
}

main() {
  parse_args "$@"

  local ts
  ts="$(date +%Y%m%d_%H%M%S)"
  local out_dir="$OUTPUT_BASE/$ts"

  log "=== SMA Audit Export ==="
  log "Container: $CONTAINER_NAME"
  log "Days:      $EXPORT_DAYS"
  log "Output:    $out_dir"

  mkdir -p "$out_dir"

  if ! ensure_prereqs; then
    log "Summary: PASS=$PASS FAIL=$FAIL"
    exit 2
  fi

  export_csv "$out_dir/audit_expectations_latest.csv" "SELECT * FROM audit_expectations_latest ORDER BY domain, code"
  export_csv "$out_dir/audit_events_${EXPORT_DAYS}d.csv" "SELECT * FROM audit_events WHERE event_time >= now() - interval '${EXPORT_DAYS} days' ORDER BY event_time DESC"
  export_csv "$out_dir/audit_cross_garage_${EXPORT_DAYS}d.csv" "SELECT * FROM audit_events WHERE cross_garage = TRUE AND event_time >= now() - interval '${EXPORT_DAYS} days' ORDER BY event_time DESC"
  export_csv "$out_dir/audit_expectation_checks.csv" "SELECT * FROM audit_expectation_checks ORDER BY checked_at DESC"

  cat > "$out_dir/README.txt" <<EOF
SMA Audit Export
Generated at: $ts
Container: $CONTAINER_NAME
Days: $EXPORT_DAYS

Files:
- audit_expectations_latest.csv
- audit_events_${EXPORT_DAYS}d.csv
- audit_cross_garage_${EXPORT_DAYS}d.csv
- audit_expectation_checks.csv
EOF

  pass "Wrote export manifest README.txt"

  log "Summary: PASS=$PASS FAIL=$FAIL"
  if [[ "$FAIL" -gt 0 ]]; then
    exit 1
  fi
}

main "$@"

