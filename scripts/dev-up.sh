#!/usr/bin/env bash
# =============================================================================
# dev-up.sh — Lance la stack en mode securise (Keycloak) ou mode rapide (sans IAM)
# =============================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/compose.yaml"
KEYCLOAK_REALM_SCRIPT="${ROOT_DIR}/scripts/keycloak-realm.sh"

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; CYAN='\033[0;36m'; NC='\033[0m'
ok()   { echo -e "${GREEN}[OK]${NC}   $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $*"; }
fail() { echo -e "${RED}[FAIL]${NC} $*"; }
info() { echo -e "${CYAN}[INFO]${NC} $*"; }

usage() {
  cat <<'EOF'
Usage: ./scripts/dev-up.sh [secure|fast|status|down|restart-secure|restart-fast|help]

  secure          Lance toute la stack avec securite active (Keycloak ON)
  fast            Lance la stack en mode dev rapide (frontend/backend sans login)
  status          Affiche l'etat des conteneurs
  down            Stoppe les conteneurs (sans supprimer les volumes)
  restart-secure  down puis secure
  restart-fast    down puis fast
  help            Affiche cette aide

Variables utiles:
  SKIP_SEED_USERS=true   N'executera pas seed-demo-users en mode secure
  COMPOSE_PROJECT_NAME   Prefixe Docker Compose (optionnel)
EOF
}

wait_keycloak_ready() {
  local max_wait="${1:-240}"
  local elapsed=0
  local probe_ok=false
  local probes=(
    "http://localhost:8080/health/ready"
    "http://localhost:8080/realms/master/.well-known/openid-configuration"
    "http://localhost:8080/realms/sma-realm"
  )

  info "Attente de Keycloak (probes: /health/ready, well-known, realm) ..."

  until [[ "$probe_ok" == "true" ]]; do
    for probe in "${probes[@]}"; do
      if curl -fsS "$probe" >/dev/null 2>&1; then
        probe_ok=true
        break
      fi
    done

    if [[ "$probe_ok" == "true" ]]; then
      break
    fi

    sleep 3
    elapsed=$((elapsed + 3))
    if [[ "$elapsed" -ge "$max_wait" ]]; then
      warn "Keycloak non pret apres ${max_wait}s. La stack est lancee, mais le seed auto est saute."
      return 1
    fi
  done

  ok "Keycloak est pret"
  return 0
}

seed_demo_users_if_possible() {
  if [[ "${SKIP_SEED_USERS:-false}" == "true" ]]; then
    warn "Seed demo users saute (SKIP_SEED_USERS=true)"
    return 0
  fi

  if [[ ! -x "${KEYCLOAK_REALM_SCRIPT}" ]]; then
    chmod +x "${KEYCLOAK_REALM_SCRIPT}"
  fi

  if wait_keycloak_ready; then
    info "Creation/maj des 3 comptes de demo (ADMIN/USER/AUDITOR)"
    "${KEYCLOAK_REALM_SCRIPT}" seed-demo-users || warn "Seed demo users echoue (vous pouvez relancer manuellement)"
  fi
}

compose_up_secure() {
  info "Lancement de la stack en mode SECURE (Keycloak ON)"
  docker compose -f "${COMPOSE_FILE}" up -d
  seed_demo_users_if_possible

  echo ""
  ok "Mode SECURE actif"
  echo "  - Frontend: http://localhost:8091"
  echo "  - Keycloak: http://localhost:8080"
  echo "  - Comptes demo: admin-test / user-test / audit-test"
}

compose_up_fast() {
  info "Lancement de la stack en mode FAST (sans login Keycloak sur frontend/backend)"
  FRONTEND_SECURITY_ENABLED=false BACKEND_SECURITY_ENABLED=false docker compose -f "${COMPOSE_FILE}" up -d

  echo ""
  ok "Mode FAST actif"
  echo "  - Frontend: http://localhost:8091"
  echo "  - Keycloak reste demarre pour outillage si besoin"
}

compose_status() {
  docker compose -f "${COMPOSE_FILE}" ps
}

compose_down() {
  info "Arret de la stack (volumes conserves)"
  docker compose -f "${COMPOSE_FILE}" down
  ok "Stack arretee"
}

CMD="${1:-secure}"
case "${CMD}" in
  secure)
    compose_up_secure
    ;;
  fast)
    compose_up_fast
    ;;
  status)
    compose_status
    ;;
  down)
    compose_down
    ;;
  restart-secure)
    compose_down
    compose_up_secure
    ;;
  restart-fast)
    compose_down
    compose_up_fast
    ;;
  help|-h|--help)
    usage
    ;;
  *)
    fail "Commande inconnue: ${CMD}"
    usage
    exit 1
    ;;
esac

