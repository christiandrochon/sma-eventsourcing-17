#!/usr/bin/env bash
set -euo pipefail

#!/usr/bin/env bash
set -euo pipefail

# dev-env.sh — lance le backend en dev en important le .env local non commité
# Déplacé depuis backend/dev-run.sh pour être dans le dossier scripts/

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND_DIR="${ROOT_DIR}/backend"

if [ -f "${BACKEND_DIR}/.env" ]; then
  set -a
  # shellcheck disable=SC1091
  source "${BACKEND_DIR}/.env"
  set +a
else
  echo "Warning: ${BACKEND_DIR}/.env not found. Using existing environment variables."
  echo "Create ${BACKEND_DIR}/.env from .env.example and do not commit it."
fi

cd "${BACKEND_DIR}"

# Lancer Spring Boot en profile local
./mvnw -Dspring-boot.run.profiles=local spring-boot:run

