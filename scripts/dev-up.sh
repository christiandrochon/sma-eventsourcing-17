#!/usr/bin/env bash
# =============================================================================
# dev-up.sh — Lance la stack en mode securise (Keycloak) ou mode rapide (sans IAM)
# =============================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/compose.yaml"
KEYCLOAK_REALM_SCRIPT="${ROOT_DIR}/scripts/keycloak-realm.sh"
AUDIT_SCHEMA_SQL="${ROOT_DIR}/docker/audit_schema.sql"
PROJECT_NAME="${COMPOSE_PROJECT_NAME:-}"

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; CYAN='\033[0;36m'; NC='\033[0m'
ok()   { echo -e "${GREEN}[OK]${NC}   $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $*"; }
fail() { echo -e "${RED}[FAIL]${NC} $*"; }
info() { echo -e "${CYAN}[INFO]${NC} $*"; }

compose_cmd() {
  if [[ -n "${PROJECT_NAME}" ]]; then
    docker compose -p "${PROJECT_NAME}" -f "${COMPOSE_FILE}" "$@"
  else
    docker compose -f "${COMPOSE_FILE}" "$@"
  fi
}

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

wait_postgres_ready() {
  local service="$1"
  local user="$2"
  local db="$3"
  local port="${4:-5432}"
  local max_wait="${5:-180}"
  local elapsed=0

  info "Attente de PostgreSQL (${service}) ..."
  until compose_cmd exec -T "${service}" pg_isready -U "${user}" -d "${db}" -p "${port}" >/dev/null 2>&1; do
    sleep 2
    elapsed=$((elapsed + 2))
    if [[ "${elapsed}" -ge "${max_wait}" ]]; then
      fail "PostgreSQL ${service} non pret apres ${max_wait}s"
      return 1
    fi
  done
  ok "PostgreSQL ${service} est pret"
}

ensure_database_exists() {
  local service="$1"
  local user="$2"
  local db_name="$3"
  local port="${4:-5432}"
  local exists

  exists=$(compose_cmd exec -T "${service}" psql -U "${user}" -p "${port}" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='${db_name}'" 2>/dev/null || true)
  if [[ "${exists}" == "1" ]]; then
    ok "Base '${db_name}' deja presente (${service})"
    return 0
  fi

  info "Creation de la base '${db_name}' dans ${service}"
  compose_cmd exec -T "${service}" psql -U "${user}" -p "${port}" -d postgres -c "CREATE DATABASE \"${db_name}\";" >/dev/null
  ok "Base '${db_name}' creee (${service})"
}

bootstrap_databases() {
  wait_postgres_ready "postgres-monolithe" "postgres" "postgres" "5432"
  wait_postgres_ready "postgres-keycloak" "keycloak" "postgres" "5433"

  # Garantit les BD attendues meme si le volume existant a ete initialise autrement.
  ensure_database_exists "postgres-monolithe" "postgres" "monolithe" "5432"
  ensure_database_exists "postgres-monolithe" "postgres" "audit" "5432"
  ensure_database_exists "postgres-keycloak" "keycloak" "keycloak" "5433"

  if [[ -f "${AUDIT_SCHEMA_SQL}" ]]; then
    info "Application du schema audit (idempotent)"
    compose_cmd exec -T postgres-monolithe psql -U postgres -d audit < "${AUDIT_SCHEMA_SQL}" >/dev/null
    ok "Schema audit verifie"
  else
    warn "Schema audit introuvable: ${AUDIT_SCHEMA_SQL}"
  fi
}

bring_up_infra() {
  info "Demarrage de l'infrastructure (PostgreSQL, Axon, pgAdmin)"
  compose_cmd up -d postgres-monolithe postgres-keycloak axon-server pgadmin4
  bootstrap_databases
}

restore_realm_if_needed() {
  if [[ ! -x "${KEYCLOAK_REALM_SCRIPT}" ]]; then
    chmod +x "${KEYCLOAK_REALM_SCRIPT}"
  fi
  info "Verification/import du realm Keycloak"
  "${KEYCLOAK_REALM_SCRIPT}" restore
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
    restore_realm_if_needed
    info "Creation/maj des 3 comptes de demo (ADMIN/USER/AUDITOR)"

    local tries=0
    local max_tries=3
    until "${KEYCLOAK_REALM_SCRIPT}" seed-demo-users; do
      tries=$((tries + 1))
      if [[ "${tries}" -ge "${max_tries}" ]]; then
        fail "Seed demo users echoue apres ${max_tries} tentatives"
        return 1
      fi
      warn "Seed echoue, nouvelle tentative (${tries}/${max_tries}) ..."
      sleep 4
    done
    ok "Comptes demo Keycloak verifies"
  fi
}

compose_up_secure() {
  info "Lancement de la stack en mode SECURE (Keycloak ON)"
  bring_up_infra
  compose_cmd up -d --build keycloak backend frontend
  seed_demo_users_if_possible

  echo ""
  ok "Mode SECURE actif"
  echo "  - Frontend: http://localhost:8091"
  echo "  - Keycloak: http://localhost:8080"
  echo "  - Comptes demo: admin-test / user-test / audit-test"
}

compose_up_fast() {
  info "Lancement de la stack en mode FAST (sans login Keycloak sur frontend/backend)"
  bring_up_infra
  FRONTEND_SECURITY_ENABLED=false BACKEND_SECURITY_ENABLED=false compose_cmd up -d --build keycloak backend frontend

  echo ""
  ok "Mode FAST actif"
  echo "  - Frontend: http://localhost:8091"
  echo "  - Keycloak reste demarre pour outillage si besoin"
}

compose_status() {
  compose_cmd ps
}

compose_down() {
  info "Arret de la stack (volumes conserves)"
  compose_cmd down
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

