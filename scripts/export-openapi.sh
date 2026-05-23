#!/usr/bin/env bash
set -euo pipefail

API_DOCS_URL_DEFAULT="http://localhost:8092/v3/api-docs"
MODE="local"
API_DOCS_URL="${API_DOCS_URL_DEFAULT}"

print_help() {
  cat <<'EOF'
Usage: ./scripts/export-openapi.sh [options]

Exporte la spec OpenAPI du backend.

Options:
  --mode local|canonical  local: export en racine (fichiers ignores)
                          canonical: export versionne dans docs/openapi/
  --url URL               URL du endpoint OpenAPI (defaut: http://localhost:8092/v3/api-docs)
  -h, --help              Affiche cette aide

Exemples:
  ./scripts/export-openapi.sh
  ./scripts/export-openapi.sh --mode canonical
  ./scripts/export-openapi.sh --url http://localhost:8092/v3/api-docs --mode canonical
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --mode)
      if [[ $# -lt 2 ]]; then
        echo "Erreur: --mode requiert une valeur (local|canonical)." >&2
        exit 1
      fi
      MODE="$2"
      shift 2
      ;;
    --url)
      if [[ $# -lt 2 ]]; then
        echo "Erreur: --url requiert une valeur." >&2
        exit 1
      fi
      API_DOCS_URL="$2"
      shift 2
      ;;
    -h|--help)
      print_help
      exit 0
      ;;
    *)
      echo "Option inconnue: $1" >&2
      print_help
      exit 1
      ;;
  esac
done

if [[ "${MODE}" != "local" && "${MODE}" != "canonical" ]]; then
  echo "Erreur: mode invalide '${MODE}' (attendu: local|canonical)." >&2
  exit 1
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ "${MODE}" == "canonical" ]]; then
  OUT_JSON="${ROOT_DIR}/docs/openapi/openapi.json"
  OUT_YAML="${ROOT_DIR}/docs/openapi/openapi.yaml"
else
  OUT_JSON="${ROOT_DIR}/openapi.json"
  OUT_YAML="${ROOT_DIR}/openapi.yaml"
fi

mkdir -p "$(dirname "${OUT_JSON}")"

echo "Export OpenAPI depuis: ${API_DOCS_URL}"

curl -fsS "${API_DOCS_URL}" -o "${OUT_JSON}"
curl -fsS "${API_DOCS_URL}.yaml" -o "${OUT_YAML}"

echo "OK JSON : ${OUT_JSON}"
echo "OK YAML : ${OUT_YAML}"

