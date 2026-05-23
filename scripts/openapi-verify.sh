#!/usr/bin/env bash
# Verifie la presence du contrat OpenAPI canonique et optionnellement compare la spec live a la version versionnee.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CANONICAL_JSON="${ROOT_DIR}/docs/openapi/openapi.json"
LIVE_URL="${LIVE_URL:-http://localhost:8092/v3/api-docs}"
MODE="${1:-canonical-only}"
TMP_JSON="$(mktemp)"

cleanup() {
  rm -f "${TMP_JSON}"
}
trap cleanup EXIT

if [[ ! -f "${CANONICAL_JSON}" ]]; then
  echo "[FAIL] Fichier manquant: ${CANONICAL_JSON}"
  exit 1
fi

python3 - <<'PY' "${CANONICAL_JSON}"
import json, sys
path = sys.argv[1]
with open(path, encoding='utf-8') as f:
    data = json.load(f)
if 'openapi' not in data:
    raise SystemExit('[FAIL] openapi key missing in canonical file')
print('[OK] Canonical OpenAPI version:', data['openapi'])
PY

if [[ "${MODE}" == "canonical-only" ]]; then
  exit 0
fi

curl -fsS "${LIVE_URL}" -o "${TMP_JSON}"

if diff -u "${CANONICAL_JSON}" "${TMP_JSON}" >/dev/null; then
  echo "[OK] Live OpenAPI == canonical"
else
  echo "[FAIL] Live OpenAPI differs from canonical"
  echo "Tip: ./scripts/export-openapi.sh --mode canonical"
  exit 1
fi

