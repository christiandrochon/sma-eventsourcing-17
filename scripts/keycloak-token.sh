#!/usr/bin/env bash
# =============================================================================
# keycloak-token.sh — Récupère un JWT depuis Keycloak sans login interactif
# Utilité : Obtenir un token pour utiliser dans des scripts automatisés ou appels curl
# Supports : client_credentials (service account) et password grant (dev/fallback)
# Résultat : Affiche le JWT pur (à utiliser dans -H "Authorization: Bearer $TOKEN")
# À exécuter : Depuis d'autres scripts (audit-load-test.sh, audit-external-40.sh, etc.)
# =============================================================================
set -euo pipefail


KEYCLOAK_BASE_URL="${KEYCLOAK_BASE_URL:-http://localhost:8080}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-sma-realm}"
KEYCLOAK_CLIENT_ID="${KEYCLOAK_CLIENT_ID:-sma-thymeleaf-frontend}"
KEYCLOAK_CLIENT_SECRET="${KEYCLOAK_CLIENT_SECRET:-}"
KEYCLOAK_USERNAME="${KEYCLOAK_USERNAME:-}"
KEYCLOAK_PASSWORD="${KEYCLOAK_PASSWORD:-}"
KEYCLOAK_GRANT_TYPE="${KEYCLOAK_GRANT_TYPE:-client_credentials}"

TOKEN_URL="$KEYCLOAK_BASE_URL/realms/$KEYCLOAK_REALM/protocol/openid-connect/token"

if ! command -v curl >/dev/null 2>&1; then
  echo "curl command not found" >&2
  exit 2
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo "python3 command not found" >&2
  exit 2
fi

payload=(
  -sS
  -X POST "$TOKEN_URL"
  -H "Content-Type: application/x-www-form-urlencoded"
  -d "client_id=$KEYCLOAK_CLIENT_ID"
  -d "grant_type=$KEYCLOAK_GRANT_TYPE"
)

if [[ -n "$KEYCLOAK_CLIENT_SECRET" ]]; then
  payload+=( -d "client_secret=$KEYCLOAK_CLIENT_SECRET" )
fi

if [[ "$KEYCLOAK_GRANT_TYPE" == "password" ]]; then
  if [[ -z "$KEYCLOAK_USERNAME" || -z "$KEYCLOAK_PASSWORD" ]]; then
    echo "For grant_type=password, KEYCLOAK_USERNAME and KEYCLOAK_PASSWORD are required" >&2
    exit 2
  fi
  payload+=( -d "username=$KEYCLOAK_USERNAME" -d "password=$KEYCLOAK_PASSWORD" )
fi

response="$(curl "${payload[@]}")"

python3 - <<'PY' "$response"
import json,sys
raw=sys.argv[1]
try:
    data=json.loads(raw)
except Exception:
    print(raw)
    sys.exit(1)
if "access_token" not in data:
    print(json.dumps(data, indent=2))
    sys.exit(1)
print(data["access_token"])
PY

