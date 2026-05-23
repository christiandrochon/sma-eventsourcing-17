#!/usr/bin/env bash
# Smoke-check post-deploiement: verifie frontend, backend, keycloak et swagger selon le profil.
set -euo pipefail

FRONTEND_URL="${FRONTEND_URL:-http://localhost:8091}"
BACKEND_URL="${BACKEND_URL:-http://localhost:8092}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8080}"
SWAGGER_URL="${SWAGGER_URL:-${BACKEND_URL}/swagger-ui.html}"
HEALTH_URL="${HEALTH_URL:-${BACKEND_URL}/actuator/health/liveness}"
PROFILE="${PROFILE:-dev}"

check_url() {
  local name="$1"
  local url="$2"
  local expected_regex="${3:-^(200|301|302)$}"
  local code

  code="$(curl -s -o /dev/null -w '%{http_code}' "$url" || true)"
  if [[ "$code" =~ $expected_regex ]]; then
    echo "[OK] ${name}: ${url} (HTTP ${code})"
  else
    echo "[FAIL] ${name}: ${url} (HTTP ${code})"
    return 1
  fi
}

echo "Smoke-check profile=${PROFILE}"
check_url "Frontend" "${FRONTEND_URL}"
check_url "Backend liveness" "${HEALTH_URL}"
check_url "Keycloak well-known" "${KEYCLOAK_URL}/realms/sma-realm/.well-known/openid-configuration"

if [[ "${PROFILE}" == "prod" ]]; then
  # En prod la doc est desactivee par defaut.
  check_url "Swagger (prod expected disabled)" "${SWAGGER_URL}" "^(401|403|404)$"
else
  # En dev la doc doit rester consultable.
  check_url "Swagger (dev expected reachable)" "${SWAGGER_URL}" "^(200|301|302)$"
fi

echo "Smoke-check termine"
