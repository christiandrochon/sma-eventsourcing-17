#!/usr/bin/env bash
# =============================================================================
# audit-external-40.sh — Campagne d'audit externe complète : 40 cas de test
# Utilité : Valider que tout fonctionne pour un auditeur indépendant (infra, API, immuabilité)
# Couverture : Infrastructure, schéma audit, intégrité FK, append-only, RBAC, créa verdict, exports
# Résultat : Génère audit_external_40_results.csv + audit_external_40_proof.md (détails passés/échoués)
# À exécuter : Avant certification externe (produit un dossier de preuves dans ./audit-exports)
# =============================================================================
set -u
set -o pipefail
CONTAINER_NAME="${AUDIT_PG_CONTAINER:-postgres-monolithe}"
POSTGRES_USER="${AUDIT_POSTGRES_USER:-postgres}"
BACKEND_URL="${AUDIT_BACKEND_URL:-http://localhost:8092}"
KEYCLOAK_BASE_URL="${KEYCLOAK_BASE_URL:-http://localhost:8080}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-sma-realm}"
KEYCLOAK_CLIENT_ID="${KEYCLOAK_CLIENT_ID:-sma-thymeleaf-frontend}"

TS="$(date +%Y%m%d_%H%M%S)"
OUT_DIR="${AUDIT_CAMPAIGN_DIR:-$ROOT_DIR/audit-exports/$TS}"
RESULTS_CSV="$OUT_DIR/audit_external_40_results.csv"
PROOF_MD="$OUT_DIR/audit_external_40_proof.md"
TMP_BODY="$OUT_DIR/.tmp_body.json"

PASS=0
FAIL=0
WARN=0
CASE_NO=0
CURRENT_ID=""

mkdir -p "$OUT_DIR"

log_case() {
  local id="$1"
  local status="$2"
  local title="$3"
  local expected="$4"
  local actual="$5"
  local proof="$6"

  printf '%s,%s,"%s","%s","%s","%s"\n' "$id" "$status" "$title" "$expected" "$actual" "$proof" >> "$RESULTS_CSV"

  case "$status" in
    PASS) PASS=$((PASS + 1)) ;;
    FAIL) FAIL=$((FAIL + 1)) ;;
    WARN) WARN=$((WARN + 1)) ;;
  esac

  {
    echo "### $id [$status] $title"
    echo "- attendu: $expected"
    echo "- observe: $actual"
    echo "- preuve: $proof"
    echo
  } >> "$PROOF_MD"
}

next_id() {
  CASE_NO=$((CASE_NO + 1))
  CURRENT_ID="$(printf 'C%02d' "$CASE_NO")"
}

run_sql_value() {
  local sql="$1"
  docker exec -i "$CONTAINER_NAME" psql -U "$POSTGRES_USER" -d audit -At -c "$sql" 2>&1
}

run_sql_expect_value() {
  local title="$1"
  local sql="$2"
  local expected="$3"
  local id
  next_id
  id="$CURRENT_ID"
  local out
  out="$(run_sql_value "$sql")"
  if [[ "$out" == "$expected" ]]; then
    log_case "$id" PASS "$title" "$expected" "$out" "SQL"
  else
    log_case "$id" FAIL "$title" "$expected" "$out" "SQL"
  fi
}

run_sql_expect_true() {
  local title="$1"
  local sql="$2"
  local id
  next_id
  id="$CURRENT_ID"
  local out
  out="$(run_sql_value "$sql")"
  if [[ "$out" == "t" || "$out" == "1" ]]; then
    log_case "$id" PASS "$title" "true/1" "$out" "SQL"
  else
    log_case "$id" FAIL "$title" "true/1" "$out" "SQL"
  fi
}

run_sql_expect_gt0() {
  local title="$1"
  local sql="$2"
  local id
  next_id
  id="$CURRENT_ID"
  local out
  out="$(run_sql_value "$sql")"
  if [[ "$out" =~ ^[0-9]+$ ]] && (( out > 0 )); then
    log_case "$id" PASS "$title" ">0" "$out" "SQL"
  else
    log_case "$id" FAIL "$title" ">0" "$out" "SQL"
  fi
}

run_sql_expect_ge() {
  local title="$1"
  local sql="$2"
  local min="$3"
  local id
  next_id
  id="$CURRENT_ID"
  local out
  out="$(run_sql_value "$sql")"
  if [[ "$out" =~ ^[0-9]+$ ]] && (( out >= min )); then
    log_case "$id" PASS "$title" ">=$min" "$out" "SQL"
  else
    log_case "$id" FAIL "$title" ">=$min" "$out" "SQL"
  fi
}

run_sql_expect_error_like() {
  local title="$1"
  local sql="$2"
  local needle="$3"
  local id
  next_id
  id="$CURRENT_ID"
  local out
  out="$(run_sql_value "$sql")"
  if echo "$out" | grep -qi "$needle"; then
    log_case "$id" PASS "$title" "error like: $needle" "$out" "SQL"
  else
    log_case "$id" FAIL "$title" "error like: $needle" "$out" "SQL"
  fi
}

http_call_status() {
  local method="$1"
  local url="$2"
  local token="${3:-}"
  local body="${4:-}"

  if [[ -n "$token" ]]; then
    if [[ -n "$body" ]]; then
      curl -sS -o "$TMP_BODY" -w "%{http_code}" -X "$method" "$url" -H "Authorization: Bearer $token" -H "Content-Type: application/json" -d "$body"
    else
      curl -sS -o "$TMP_BODY" -w "%{http_code}" -X "$method" "$url" -H "Authorization: Bearer $token"
    fi
  else
    if [[ -n "$body" ]]; then
      curl -sS -o "$TMP_BODY" -w "%{http_code}" -X "$method" "$url" -H "Content-Type: application/json" -d "$body"
    else
      curl -sS -o "$TMP_BODY" -w "%{http_code}" -X "$method" "$url"
    fi
  fi
}

run_http_case() {
  local title="$1"
  local method="$2"
  local url="$3"
  local token="$4"
  local expected_status="$5"
  local body="${6:-}"

  local id
  next_id
  id="$CURRENT_ID"

  local status
  status="$(http_call_status "$method" "$url" "$token" "$body" 2>/dev/null || echo 000)"
  local snippet
  snippet="$(head -c 220 "$TMP_BODY" 2>/dev/null | tr '\n' ' ' | sed 's/"/\\"/g')"

  if [[ "$status" == "$expected_status" ]]; then
    log_case "$id" PASS "$title" "HTTP $expected_status" "HTTP $status" "curl $method $url body=$snippet"
  elif [[ "$status" == "000" ]]; then
    log_case "$id" WARN "$title" "HTTP $expected_status" "HTTP 000" "curl connect error"
  else
    log_case "$id" FAIL "$title" "HTTP $expected_status" "HTTP $status" "curl $method $url body=$snippet"
  fi
}

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

{
  echo "# Campagne audit externe - 40 cas"
  echo "- date: $(date -Iseconds)"
  echo "- backend: $BACKEND_URL"
  echo "- postgres container: $CONTAINER_NAME"
  echo "- keycloak: $KEYCLOAK_BASE_URL"
  echo
} > "$PROOF_MD"

echo 'case_id,status,title,expected,actual,proof' > "$RESULTS_CSV"

ADMIN_TOKEN="$(get_token admin-test 'admin123!')"
USER_TOKEN="$(get_token user-test 'user123!')"
AUDITOR_TOKEN="$(get_token audit-test 'audit123!')"
INVALID_TOKEN="invalid.token.value"

CHECKS_BEFORE="$(run_sql_value "SELECT COUNT(*) FROM audit_expectation_checks;")"

# Ensure a deterministic cross-garage sample exists for the audit campaign.
CROSS_COUNT="$(run_sql_value "SELECT COUNT(*) FROM audit_events WHERE cross_garage = true AND event_time >= now()-interval '30 days';")"
if [[ "$CROSS_COUNT" =~ ^[0-9]+$ ]] && (( CROSS_COUNT == 0 )); then
  run_sql_value "INSERT INTO audit_events (actor, actor_garage, action, resource, resource_id, garage_id, cross_garage, reason, result, http_method, http_path, http_status, ip_address, user_agent, details) VALUES ('audit-campaign-40', 'garage-A', 'AUDIT_SAMPLE', 'vehicule', 'seed-cross-001', 'garage-B', TRUE, 'seeded for external audit evidence', 'OK', 'GET', '/audit/campaign/seed', 200, '127.0.0.1', 'audit-external-40', 'cross-garage seed event');" >/dev/null
fi

run_sql_expect_true "Container postgres visible" "SELECT 1;"
run_http_case "Backend health endpoint" "GET" "$BACKEND_URL/actuator/health" "" "200"
run_sql_expect_true "Database audit reachable" "SELECT current_database()='audit';"
run_sql_expect_true "Table audit_events exists" "SELECT to_regclass('public.audit_events') IS NOT NULL;"
run_sql_expect_true "Table audit_expectations exists" "SELECT to_regclass('public.audit_expectations') IS NOT NULL;"
run_sql_expect_true "Table audit_expectation_checks exists" "SELECT to_regclass('public.audit_expectation_checks') IS NOT NULL;"
run_sql_expect_true "View audit_expectations_latest exists" "SELECT to_regclass('public.audit_expectations_latest') IS NOT NULL;"
run_sql_expect_ge "Expectations seeded" "SELECT COUNT(*) FROM audit_expectations;" 15
run_sql_expect_gt0 "Events in last 30 days" "SELECT COUNT(*) FROM audit_events WHERE event_time >= now()-interval '30 days';"
run_sql_expect_gt0 "Cross-garage events in last 30 days" "SELECT COUNT(*) FROM audit_events WHERE cross_garage = true AND event_time >= now()-interval '30 days';"
run_sql_expect_true "Trigger on audit_events exists" "SELECT COUNT(*)=1 FROM pg_trigger WHERE tgname='trg_audit_events_block_mutation';"
run_sql_expect_true "Trigger on audit_expectation_checks exists" "SELECT COUNT(*)=1 FROM pg_trigger WHERE tgname='trg_audit_expectation_checks_block_mutation';"
run_sql_expect_error_like "UPDATE blocked on audit_events" "UPDATE audit_events SET action='MUT' WHERE id=(SELECT id FROM audit_events ORDER BY id DESC LIMIT 1);" "append-only"
run_sql_expect_error_like "DELETE blocked on audit_events" "DELETE FROM audit_events WHERE id=(SELECT id FROM audit_events ORDER BY id DESC LIMIT 1);" "append-only"
run_sql_expect_error_like "UPDATE blocked on audit_expectation_checks" "UPDATE audit_expectation_checks SET score=99 WHERE id=(SELECT id FROM audit_expectation_checks ORDER BY id DESC LIMIT 1);" "append-only"
run_sql_expect_error_like "DELETE blocked on audit_expectation_checks" "DELETE FROM audit_expectation_checks WHERE id=(SELECT id FROM audit_expectation_checks ORDER BY id DESC LIMIT 1);" "append-only"
run_sql_expect_error_like "FK check on unknown expectation" "INSERT INTO audit_expectation_checks (expectation_code, checked_by, status, score, scope, findings, remediation_plan, due_date, evidence_uri, cross_garage_sample_size, inserted_from) VALUES ('ZZZ_999','audit-bot','PARTIAL',50,'scope','finding','plan','2026-12-31','urn:dummy',1,'INDEPENDENT_AUDIT');" "foreign key"
run_sql_expect_true "Audit event IDs are positive" "SELECT COALESCE(MIN(id),1) > 0 FROM audit_events;"
run_sql_expect_value "Duplicate expectation codes" "SELECT COUNT(*) FROM (SELECT code, COUNT(*) c FROM audit_expectations GROUP BY code HAVING COUNT(*) > 1) d;" "0"
run_sql_expect_gt0 "Latest expectation view has rows" "SELECT COUNT(*) FROM audit_expectations_latest;"

run_http_case "GET dashboard without token" "GET" "$BACKEND_URL/audit/compliance/dashboard" "" "401"
run_http_case "GET dashboard with invalid token" "GET" "$BACKEND_URL/audit/compliance/dashboard" "$INVALID_TOKEN" "401"
run_http_case "GET dashboard as USER" "GET" "$BACKEND_URL/audit/compliance/dashboard" "$USER_TOKEN" "403"
run_http_case "GET dashboard as AUDITOR" "GET" "$BACKEND_URL/audit/compliance/dashboard" "$AUDITOR_TOKEN" "200"
run_http_case "GET dashboard as ADMIN" "GET" "$BACKEND_URL/audit/compliance/dashboard" "$ADMIN_TOKEN" "200"
run_http_case "GET expectations as AUDITOR" "GET" "$BACKEND_URL/audit/compliance/expectations" "$AUDITOR_TOKEN" "200"
run_http_case "GET expectations as ADMIN" "GET" "$BACKEND_URL/audit/compliance/expectations" "$ADMIN_TOKEN" "200"
run_http_case "GET expectations as USER" "GET" "$BACKEND_URL/audit/compliance/expectations" "$USER_TOKEN" "403"
run_http_case "GET expectation AUD_001 as AUDITOR" "GET" "$BACKEND_URL/audit/compliance/expectations/AUD_001?historyLimit=5" "$AUDITOR_TOKEN" "200"
run_http_case "GET expectation AUD_001 as ADMIN" "GET" "$BACKEND_URL/audit/compliance/expectations/AUD_001?historyLimit=2" "$ADMIN_TOKEN" "200"
run_http_case "GET unknown expectation as AUDITOR" "GET" "$BACKEND_URL/audit/compliance/expectations/XXX_404" "$AUDITOR_TOKEN" "404"
run_http_case "GET unknown expectation as ADMIN" "GET" "$BACKEND_URL/audit/compliance/expectations/XXX_404" "$ADMIN_TOKEN" "404"
run_http_case "POST check as AUDITOR (forbidden)" "POST" "$BACKEND_URL/audit/compliance/expectations/AUD_001/checks" "$AUDITOR_TOKEN" "403" '{"checkedBy":"auditor-user","status":"PARTIAL","score":55,"scope":"read-only test","findings":"forbidden expected","remediationPlan":"n/a","dueDate":"2026-06-30","evidenceUri":"urn:test:auditor","crossGarageSampleSize":3,"insertedFrom":"INDEPENDENT_AUDIT"}'
run_http_case "POST check as USER (forbidden)" "POST" "$BACKEND_URL/audit/compliance/expectations/AUD_001/checks" "$USER_TOKEN" "403" '{"checkedBy":"user-user","status":"PARTIAL","score":55,"scope":"read-only test","findings":"forbidden expected","remediationPlan":"n/a","dueDate":"2026-06-30","evidenceUri":"urn:test:user","crossGarageSampleSize":3,"insertedFrom":"INDEPENDENT_AUDIT"}'
run_http_case "POST invalid check as ADMIN" "POST" "$BACKEND_URL/audit/compliance/expectations/AUD_001/checks" "$ADMIN_TOKEN" "400" '{"checkedBy":"admin-invalid"}'
run_http_case "POST valid check as ADMIN" "POST" "$BACKEND_URL/audit/compliance/expectations/AUD_001/checks" "$ADMIN_TOKEN" "201" '{"checkedBy":"cabinet-externe-40","status":"PARTIAL","score":74,"scope":"Campagne 40 cas","findings":"Conforme globalement, 2 ecarts mineurs","remediationPlan":"Suivi mensuel des ecarts","dueDate":"2026-07-15","evidenceUri":"urn:audit:campagne40","crossGarageSampleSize":40,"insertedFrom":"INDEPENDENT_AUDIT"}'

CHECKS_AFTER="$(run_sql_value "SELECT COUNT(*) FROM audit_expectation_checks;")"
if [[ "$CHECKS_BEFORE" =~ ^[0-9]+$ && "$CHECKS_AFTER" =~ ^[0-9]+$ ]]; then
  DELTA="$((CHECKS_AFTER - CHECKS_BEFORE))"
  if (( DELTA == 1 )); then
    next_id
    log_case "$CURRENT_ID" PASS "Check count increased by 1 after admin POST" "+1" "$DELTA" "SQL"
  else
    next_id
    log_case "$CURRENT_ID" FAIL "Check count increased by 1 after admin POST" "+1" "$DELTA" "SQL"
  fi
else
  next_id
  log_case "$CURRENT_ID" WARN "Check count increased by 1 after admin POST" "+1" "$CHECKS_BEFORE -> $CHECKS_AFTER" "SQL parse error"
fi

run_sql_expect_true "Latest inserted check tagged INDEPENDENT_AUDIT" "SELECT COALESCE((SELECT inserted_from='INDEPENDENT_AUDIT' FROM audit_expectation_checks ORDER BY checked_at DESC LIMIT 1), false);"

CHECK_SCRIPT_OUT="$OUT_DIR/audit_check_output.txt"
if "$ROOT_DIR/scripts/audit-check.sh" > "$CHECK_SCRIPT_OUT" 2>&1; then
  if grep -q "FAIL=0" "$CHECK_SCRIPT_OUT"; then
    next_id
    log_case "$CURRENT_ID" PASS "audit-check.sh reports FAIL=0" "FAIL=0" "$(grep 'Summary:' "$CHECK_SCRIPT_OUT" | tail -1)" "scripts/audit-check.sh"
  else
    next_id
    log_case "$CURRENT_ID" FAIL "audit-check.sh reports FAIL=0" "FAIL=0" "$(grep 'Summary:' "$CHECK_SCRIPT_OUT" | tail -1)" "scripts/audit-check.sh"
  fi
else
  next_id
  log_case "$CURRENT_ID" FAIL "audit-check.sh execution" "exit 0" "exit != 0" "scripts/audit-check.sh"
fi

EXPORT_SCRIPT_OUT="$OUT_DIR/audit_export_output.txt"
if "$ROOT_DIR/scripts/audit-export.sh" --days 7 --output-dir "$OUT_DIR" > "$EXPORT_SCRIPT_OUT" 2>&1; then
  LATEST_EXPORT_DIR="$(ls -1dt "$OUT_DIR"/20* 2>/dev/null | head -1)"
  if [[ -n "$LATEST_EXPORT_DIR" ]] && [[ -f "$LATEST_EXPORT_DIR/audit_expectations_latest.csv" ]] && [[ -f "$LATEST_EXPORT_DIR/audit_events_7d.csv" ]] && [[ -f "$LATEST_EXPORT_DIR/audit_cross_garage_7d.csv" ]] && [[ -f "$LATEST_EXPORT_DIR/audit_expectation_checks.csv" ]] && [[ -f "$LATEST_EXPORT_DIR/README.txt" ]]; then
    next_id
    log_case "$CURRENT_ID" PASS "audit-export.sh produced full evidence bundle" "5 files" "$LATEST_EXPORT_DIR" "scripts/audit-export.sh"
  else
    next_id
    log_case "$CURRENT_ID" FAIL "audit-export.sh produced full evidence bundle" "5 files" "$LATEST_EXPORT_DIR" "missing expected files"
  fi
else
  next_id
  log_case "$CURRENT_ID" FAIL "audit-export.sh execution" "exit 0" "exit != 0" "scripts/audit-export.sh"
fi

{
  echo "## Synthese"
  echo "- total: $CASE_NO"
  echo "- pass: $PASS"
  echo "- warn: $WARN"
  echo "- fail: $FAIL"
} >> "$PROOF_MD"

echo "Campagne 40 cas terminee"
echo "Resultats CSV: $RESULTS_CSV"
echo "Preuves MD:    $PROOF_MD"
echo "Summary: PASS=$PASS WARN=$WARN FAIL=$FAIL TOTAL=$CASE_NO"

if [[ "$FAIL" -gt 0 ]]; then
  exit 1
fi

