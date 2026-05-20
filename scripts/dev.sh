#!/usr/bin/env bash
# =============================================================================
# dev.sh — Point d'entrée unique pour lancer la stack et accéder à l'appli
# Utilité : Façade simplifiée qui délègue à dev-up.sh (stack) et dev-login.sh (accès)
# Commandes : secure/fast (lancer), down (arrêter), open/show/check (afficher URLs), seed-users (créer comptes)
# À exécuter : Toujours celui-ci en premier (./scripts/dev.sh secure)
# =============================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

DEV_UP_SCRIPT="${ROOT_DIR}/scripts/dev-up.sh"
DEV_LOGIN_SCRIPT="${ROOT_DIR}/scripts/dev-login.sh"
REALM_SCRIPT="${ROOT_DIR}/scripts/keycloak-realm.sh"
BACKEND_DEV_ENV_SCRIPT="${ROOT_DIR}/scripts/dev-env.sh"

usage() {
  cat <<'EOF'
Usage: ./scripts/dev.sh <commande>

Run stack:
  secure           Lance la stack avec Keycloak (delegue a dev-up.sh secure)
  fast             Lance la stack sans login frontend/backend (delegue a dev-up.sh fast)
  status           Affiche l'etat des conteneurs
  down             Arrete la stack
  restart-secure   Redemarre en mode secure
  restart-fast     Redemarre en mode fast
    run-backend      Lance le backend localement (mvn spring-boot:run, profile local)

Acces / check:
  show             Affiche URLs + credentials (delegue a dev-login.sh show)
  open             Affiche URLs + credentials et ouvre le navigateur
  check            Affiche URLs + credentials + check HTTP rapide

Keycloak users:
  seed-users       Cree/maj les 3 comptes demo (ADMIN/USER/AUDITOR)
  ensure-users     Alias de seed-users (commande la plus explicite)
  realm-status     Statut Keycloak/realm/roles/users

Aide:
  help             Affiche cette aide

Exemples:
  ./scripts/dev.sh secure
  ./scripts/dev.sh open
  ./scripts/dev.sh seed-users
  ./scripts/dev.sh ensure-users
EOF
}

ensure_script() {
  local path="$1"
  if [[ ! -x "$path" ]]; then
    chmod +x "$path"
  fi
}

main() {
  local cmd="${1:-help}"

  ensure_script "$DEV_UP_SCRIPT"
  ensure_script "$DEV_LOGIN_SCRIPT"
  ensure_script "$REALM_SCRIPT"
  ensure_script "$BACKEND_DEV_ENV_SCRIPT"

  case "$cmd" in
    secure|fast|status|down|restart-secure|restart-fast)
      exec "$DEV_UP_SCRIPT" "$cmd"
      ;;
    show|open|check)
      exec "$DEV_LOGIN_SCRIPT" "$cmd"
      ;;
    run-backend|backend)
      # Lance le backend localement en déléguant à scripts/dev-env.sh.
      # Le script `scripts/dev-env.sh` est responsable du chargement de
      # `backend/.env` (s'il existe) et du lancement de la JVM/Maven.
      exec "$BACKEND_DEV_ENV_SCRIPT" "$cmd"
      ;;
    seed-users|ensure-users)
      exec "$REALM_SCRIPT" seed-demo-users
      ;;
    realm-status)
      exec "$REALM_SCRIPT" status
      ;;
    help|-h|--help)
      usage
      ;;
    *)
      echo "Commande inconnue: $cmd" >&2
      echo "" >&2
      usage
      exit 1
      ;;
  esac
}

main "$@"
