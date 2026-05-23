#!/usr/bin/env bash
# =============================================================================
# dev-login.sh — Affiche les URLs de l'appli + credentials et peut ouvrir le navigateur
# Utilité : Affichage rapide des URLs (frontend, backend, Keycloak) avec identifiants demo
# Options : show (affichage), open (affichage + ouvre navigateur), check (affichage + test HTTP)
# Comptes : admin-test / user-test / audit-test (tous créés par dev.sh secure)
# À exécuter : Après dev.sh secure pour voir comment se connecter
# =============================================================================
set -euo pipefail

APP_URL="${APP_URL:-http://localhost:8091}"
BACKEND_HEALTH_URL="${BACKEND_HEALTH_URL:-http://localhost:8092/actuator/health}"
SWAGGER_URL="${SWAGGER_URL:-http://localhost:8092/swagger-ui/index.html}"
AXON_DASHBOARD_URL="${AXON_DASHBOARD_URL:-http://localhost:8024}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8080}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-sma-realm}"
REALM_ACCOUNT_URL="${REALM_ACCOUNT_URL:-${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/account}"
ADMIN_CONSOLE_URL="${ADMIN_CONSOLE_URL:-${KEYCLOAK_URL}/admin/master/console/}"

# Comptes de demo par defaut (alignes avec keycloak-realm.sh seed-demo-users)
DEMO_ADMIN_USER="${KC_DEMO_ADMIN_USER:-admin-test}"
DEMO_ADMIN_PASSWORD="${KC_DEMO_ADMIN_PASSWORD:-admin123!}"
DEMO_USER_USER="${KC_DEMO_USER_USER:-user-test}"
DEMO_USER_PASSWORD="${KC_DEMO_USER_PASSWORD:-user123!}"
DEMO_AUDITOR_USER="${KC_DEMO_AUDITOR_USER:-audit-test}"
DEMO_AUDITOR_PASSWORD="${KC_DEMO_AUDITOR_PASSWORD:-audit123!}"

KEYCLOAK_ADMIN_USER="${KEYCLOAK_ADMIN:-admin}"
KEYCLOAK_ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin}"

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
ok()   { echo -e "${GREEN}[OK]${NC}   $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $*"; }
info() { echo -e "${CYAN}[INFO]${NC} $*"; }

usage() {
  cat <<'EOF'
Usage: ./scripts/dev-login.sh [show|open|check|help]

  show   Affiche URLs + credentials (defaut)
  open   Affiche puis tente d'ouvrir Frontend + Swagger + Axon + Keycloak dans le navigateur
  check  Affiche un check HTTP rapide (app/backend/keycloak)
  help   Affiche cette aide

Variables utiles:
  NO_OPEN=true            N'ouvre pas le navigateur meme en mode open
  APP_URL                 URL frontend (defaut: http://localhost:8091)
  SWAGGER_URL             URL Swagger UI (defaut: http://localhost:8092/swagger-ui/index.html)
  AXON_DASHBOARD_URL      URL dashboard Axon (defaut: http://localhost:8024)
  KEYCLOAK_URL            URL Keycloak (defaut: http://localhost:8080)
  KEYCLOAK_REALM          Realm Keycloak (defaut: sma-realm)
  KC_DEMO_*               Surcharge des users/passwords demo
EOF
}

print_block() {
  echo ""
  info "=== URLs utiles ==="
  echo "  App frontend         : ${APP_URL}"
  echo "  Backend health       : ${BACKEND_HEALTH_URL}"
  echo "  Swagger UI           : ${SWAGGER_URL}"
  echo "  Axon dashboard       : ${AXON_DASHBOARD_URL}"
  echo "  Keycloak             : ${KEYCLOAK_URL}"
  echo "  Keycloak admin       : ${ADMIN_CONSOLE_URL}"
  echo "  Keycloak account     : ${REALM_ACCOUNT_URL}"

  echo ""
  info "=== Credentials Keycloak ==="
  echo "  Admin console        : ${KEYCLOAK_ADMIN_USER} / ${KEYCLOAK_ADMIN_PASSWORD}"

  echo ""
  info "=== Credentials comptes demo (RBAC) ==="
  echo "  ADMIN                : ${DEMO_ADMIN_USER} / ${DEMO_ADMIN_PASSWORD}"
  echo "  USER                 : ${DEMO_USER_USER} / ${DEMO_USER_PASSWORD}"
  echo "  AUDITOR              : ${DEMO_AUDITOR_USER} / ${DEMO_AUDITOR_PASSWORD}"

  echo ""
  warn "Si les comptes demo n'existent pas encore: ./scripts/keycloak-realm.sh seed-demo-users"
}

open_url() {
  local url="$1"
  if command -v xdg-open >/dev/null 2>&1; then
    xdg-open "$url" >/dev/null 2>&1 || true
    return 0
  fi
  if command -v gio >/dev/null 2>&1; then
    gio open "$url" >/dev/null 2>&1 || true
    return 0
  fi
  return 1
}

open_block() {
  if [[ "${NO_OPEN:-false}" == "true" ]]; then
    warn "Ouverture navigateur desactivee (NO_OPEN=true)"
    return 0
  fi

  if open_url "$APP_URL"; then
    ok "Ouverture frontend: ${APP_URL}"
  else
    warn "Impossible d'ouvrir automatiquement ${APP_URL}"
  fi

  if open_url "$SWAGGER_URL"; then
    ok "Ouverture Swagger UI: ${SWAGGER_URL}"
  else
    warn "Impossible d'ouvrir automatiquement ${SWAGGER_URL}"
  fi

  if open_url "$AXON_DASHBOARD_URL"; then
    ok "Ouverture Axon dashboard: ${AXON_DASHBOARD_URL}"
  else
    warn "Impossible d'ouvrir automatiquement ${AXON_DASHBOARD_URL}"
  fi

  if open_url "$KEYCLOAK_URL"; then
    ok "Ouverture Keycloak: ${KEYCLOAK_URL}"
  else
    warn "Impossible d'ouvrir automatiquement ${KEYCLOAK_URL}"
  fi
}

check_url() {
  local label="$1"
  local url="$2"
  local code
  code=$(curl -s -o /dev/null -w "%{http_code}" "$url" || echo "000")
  if [[ "$code" =~ ^2|3 ]]; then
    ok "${label}: ${url} (HTTP ${code})"
  else
    warn "${label}: ${url} (HTTP ${code})"
  fi
}

check_block() {
  echo ""
  info "=== Check rapide disponibilite HTTP ==="
  check_url "Frontend" "$APP_URL"
  check_url "Backend health" "$BACKEND_HEALTH_URL"
  check_url "Swagger UI" "$SWAGGER_URL"
  check_url "Axon dashboard" "$AXON_DASHBOARD_URL"
  check_url "Keycloak" "$KEYCLOAK_URL"
}

CMD="${1:-show}"
case "$CMD" in
  show)
    print_block
    ;;
  open)
    print_block
    open_block
    ;;
  check)
    print_block
    check_block
    ;;
  help|-h|--help)
    usage
    ;;
  *)
    usage
    exit 1
    ;;
esac

