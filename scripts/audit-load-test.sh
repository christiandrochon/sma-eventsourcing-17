#!/usr/bin/env bash
# =============================================================================
# audit-load-test.sh — Génère ~180 événements d'audit en boucle sur les endpoints GET
# Utilité : Charger la table audit_events avec des événements crédibles (30+ par entité)
# Couverture : Fait 30 boucles sur /queries/{clients,vehicules,documents,dossiers} avec différents rôles
# Résultat : ~180 événements audit = preuve opérationnelle complète
# À exécuter : Après dev.sh secure (Keycloak et backend doivent tourner)
# =============================================================================
set -u
set -o pipefail


ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_URL="${AUDIT_BACKEND_URL:-http://localhost:8092}"
KEYCLOAK_BASE_URL="${KEYCLOAK_BASE_URL:-http://localhost:8080}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-sma-realm}"
KEYCLOAK_CLIENT_ID="${KEYCLOAK_CLIENT_ID:-sma-thymeleaf-frontend}"

PASS=0
FAIL=0
WARN=0

log() { printf '%s\n' "$*"; }
pass() { PASS=$((PASS + 1)); log "[PASS] $*"; }
fail() { FAIL=$((FAIL + 1)); log "[FAIL] $*"; }
warn() { WARN=$((WARN + 1)); log "[WARN] $*"; }

get_token() {
  local username="$1"
  local password="$2"
  KEYCLOAK_BASE_URL="$KEYCLOAK_BASE_URL" \
  KEYCLOAK_REALM="$KEYCLOAK_REALM" \
  KEYCLOAK_CLIENT_ID="$KEYCLOAK_CLIENT_ID" \
  KEYCLOAK_GRANT_TYPE="password" \
  KEYCLOAK_USERNAME="$username" \
  KEYCLOAK_PASSWORD="$password" \
  "$ROOT_DIR/scripts/keycloak-token.sh" 2>/dev/null || true
}

log "=== SMA Audit Load Test ==="
log "Backend: $BACKEND_URL"
log "Generating events via repeated GET queries..."
log

ADMIN_TOKEN="$(get_token admin-test 'admin123!')"
USER_TOKEN="$(get_token user-test 'user123!')"
AUDITOR_TOKEN="$(get_token audit-test 'audit123!')"

if [[ -z "$ADMIN_TOKEN" ]]; then
  fail "Could not obtain ADMIN token"
  exit 1
fi

# Count before
COUNT_BEFORE=$(docker exec postgres-monolithe psql -U postgres -d audit -At -c "SELECT COUNT(*) FROM audit_events;" 2>/dev/null || echo 0)

log "Audit events before: $COUNT_BEFORE"
log

# Iterate 30 times per endpoint to generate events
for i in {1..30}; do
  # GET clients
  curl -sS -o /dev/null -w "" -H "Authorization: Bearer $ADMIN_TOKEN" "$BACKEND_URL/queries/clients" 2>/dev/null || true

  # GET clients by ID (assuming cli-1 exists from seed)
  curl -sS -o /dev/null -w "" -H "Authorization: Bearer $ADMIN_TOKEN" "$BACKEND_URL/queries/clients/cli-1" 2>/dev/null || true

  # GET vehicules
  curl -sS -o /dev/null -w "" -H "Authorization: Bearer $ADMIN_TOKEN" "$BACKEND_URL/queries/vehicules" 2>/dev/null || true

  # GET documents
  curl -sS -o /dev/null -w "" -H "Authorization: Bearer $ADMIN_TOKEN" "$BACKEND_URL/queries/documents" 2>/dev/null || true

  # GET dossiers
  curl -sS -o /dev/null -w "" -H "Authorization: Bearer $ADMIN_TOKEN" "$BACKEND_URL/queries/dossiers" 2>/dev/null || true

  # Mix in some USER queries
  if (( i % 3 == 0 )); then
    curl -sS -o /dev/null -w "" -H "Authorization: Bearer $USER_TOKEN" "$BACKEND_URL/queries/clients" 2>/dev/null || true
    curl -sS -o /dev/null -w "" -H "Authorization: Bearer $USER_TOKEN" "$BACKEND_URL/queries/vehicules" 2>/dev/null || true
  fi

  # Mix in some AUDITOR queries
  if (( i % 5 == 0 )); then
    curl -sS -o /dev/null -w "" -H "Authorization: Bearer $AUDITOR_TOKEN" "$BACKEND_URL/queries/clients" 2>/dev/null || true
    curl -sS -o /dev/null -w "" -H "Authorization: Bearer $AUDITOR_TOKEN" "$BACKEND_URL/audit/compliance/dashboard" 2>/dev/null || true
  fi

  printf "."
done

log ""
log "Sleeping 2s for async events to flush..."
sleep 2

# Count after
COUNT_AFTER=$(docker exec postgres-monolithe psql -U postgres -d audit -At -c "SELECT COUNT(*) FROM audit_events;" 2>/dev/null || echo 0)

log "Audit events after: $COUNT_AFTER"
log "Difference: $((COUNT_AFTER - COUNT_BEFORE))"
log

if [[ "$COUNT_AFTER" -gt "$COUNT_BEFORE" ]]; then
  pass "Audit events generated: $(( COUNT_AFTER - COUNT_BEFORE )) new events"
else
  warn "No new events detected"
fi

# Show event breakdown by resource
log
log "Event breakdown by resource:"
docker exec postgres-monolithe psql -U postgres -d audit -Atc "
SELECT resource, COUNT(*) as count
FROM audit_events
WHERE event_time >= now() - interval '5 minutes'
GROUP BY resource
ORDER BY count DESC;
" 2>/dev/null || log "(unable to query breakdown)"

log
log "Summary: PASS=$PASS WARN=$WARN FAIL=$FAIL"

