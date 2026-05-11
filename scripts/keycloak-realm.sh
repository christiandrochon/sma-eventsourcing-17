#!/usr/bin/env bash
# =============================================================================
# keycloak-realm.sh — Gestion du realm Keycloak sma-realm
#
# Usage:
#   ./scripts/keycloak-realm.sh status          # Verifier que le realm est vivant
#   ./scripts/keycloak-realm.sh backup          # Exporter le realm (mise a jour realm-export.json)
#   ./scripts/keycloak-realm.sh restore         # Forcer la reimportation du realm depuis le JSON
#   ./scripts/keycloak-realm.sh create-user     # Creer un utilisateur interactivement
#   ./scripts/keycloak-realm.sh list-users      # Lister les utilisateurs du realm
#   ./scripts/keycloak-realm.sh add-role        # Assigner un role a un utilisateur
#   ./scripts/keycloak-realm.sh seed-demo-users # Creer 3 comptes (ADMIN/USER/AUDITOR)
#
# Prerequis : Docker Compose lance (keycloak + postgres-keycloak)
# =============================================================================
set -euo pipefail

KEYCLOAK_BASE_URL="${KEYCLOAK_BASE_URL:-http://localhost:8080}"
KEYCLOAK_ADMIN="${KEYCLOAK_ADMIN:-admin}"
KEYCLOAK_ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin}"
REALM="${KEYCLOAK_REALM:-sma-realm}"
REALM_JSON="$(cd "$(dirname "$0")/.." && pwd)/docker/realm-export.json"

# Couleurs
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
ok()   { echo -e "${GREEN}[OK]${NC}   $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $*"; }
fail() { echo -e "${RED}[FAIL]${NC} $*"; }
info() { echo -e "${CYAN}[INFO]${NC} $*"; }

# -------------------------------------------------------------------------
# Obtenir un token admin
# -------------------------------------------------------------------------
admin_token() {
  curl -sf \
    -d "client_id=admin-cli" \
    -d "username=${KEYCLOAK_ADMIN}" \
    -d "password=${KEYCLOAK_ADMIN_PASSWORD}" \
    -d "grant_type=password" \
    "${KEYCLOAK_BASE_URL}/realms/master/protocol/openid-connect/token" \
    | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])"
}

# -------------------------------------------------------------------------
# Baseline realm (roles + defaults) pour eviter toute config manuelle
# -------------------------------------------------------------------------
ensure_role_exists() {
  local token="$1"
  local role_name="$2"
  local role_desc="$3"
  local http

  http=$(curl -so /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer ${token}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles/${role_name}" 2>/dev/null || echo "000")

  if [[ "${http}" == "200" ]]; then
    ok "Role '${role_name}' deja present"
    return 0
  fi

  http=$(curl -so /dev/null -w "%{http_code}" \
    -X POST \
    -H "Authorization: Bearer ${token}" \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"${role_name}\",\"description\":\"${role_desc}\"}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles")

  if [[ "${http}" == "201" ]]; then
    ok "Role '${role_name}' cree"
  else
    fail "Creation role '${role_name}' echouee (HTTP ${http})"
    return 1
  fi
}

get_client_uuid() {
  local token="$1"
  local client_id="$2"

  curl -sf \
    -H "Authorization: Bearer ${token}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/clients?clientId=${client_id}" \
    | python3 -c "import sys,json; cs=json.load(sys.stdin); print(cs[0]['id']) if cs else print('')"
}

ensure_client_role_exists() {
  local token="$1"
  local client_id="$2"
  local role_name="$3"
  local role_desc="$4"
  local client_uuid http

  client_uuid=$(get_client_uuid "${token}" "${client_id}")
  [[ -n "${client_uuid}" ]] || { warn "Client '${client_id}' introuvable, role '${role_name}' saute"; return 0; }

  http=$(curl -so /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer ${token}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/clients/${client_uuid}/roles/${role_name}" 2>/dev/null || echo "000")

  if [[ "${http}" == "200" ]]; then
    ok "Client role '${client_id}:${role_name}' deja present"
    return 0
  fi

  http=$(curl -so /dev/null -w "%{http_code}" \
    -X POST \
    -H "Authorization: Bearer ${token}" \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"${role_name}\",\"description\":\"${role_desc}\"}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/clients/${client_uuid}/roles")

  if [[ "${http}" == "201" ]]; then
    ok "Client role '${client_id}:${role_name}' cree"
  else
    fail "Creation client role '${client_id}:${role_name}' echouee (HTTP ${http})"
    return 1
  fi
}

ensure_realm_composite_role() {
  local token="$1"
  local parent_role="$2"
  local child_role="$3"
  local has_child child_data http

  has_child=$(curl -sf \
    -H "Authorization: Bearer ${token}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles/${parent_role}/composites" \
    | python3 -c "import sys,json; cs=json.load(sys.stdin); print('yes' if any((not c.get('clientRole', False)) and c.get('name')=='${child_role}' for c in cs) else 'no')")

  if [[ "${has_child}" == "yes" ]]; then
    ok "Composite realm '${parent_role}' contient deja '${child_role}'"
    return 0
  fi

  child_data=$(curl -sf \
    -H "Authorization: Bearer ${token}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles/${child_role}" 2>/dev/null || echo "")
  [[ -n "${child_data}" ]] || { fail "Role realm '${child_role}' introuvable"; return 1; }

  http=$(curl -so /dev/null -w "%{http_code}" \
    -X POST \
    -H "Authorization: Bearer ${token}" \
    -H "Content-Type: application/json" \
    -d "[${child_data}]" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles/${parent_role}/composites")

  if [[ "${http}" == "204" ]]; then
    ok "Composite realm ajoute: '${child_role}' -> '${parent_role}'"
  else
    fail "Ajout composite realm '${child_role}' -> '${parent_role}' echoue (HTTP ${http})"
    return 1
  fi
}

ensure_realm_has_client_role() {
  local token="$1"
  local realm_role="$2"
  local client_id="$3"
  local client_role="$4"
  local client_uuid has_child child_data http

  client_uuid=$(get_client_uuid "${token}" "${client_id}")
  [[ -n "${client_uuid}" ]] || { warn "Client '${client_id}' introuvable, composite '${realm_role}' <- '${client_role}' saute"; return 0; }

  has_child=$(curl -sf \
    -H "Authorization: Bearer ${token}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles/${realm_role}/composites" \
    | python3 -c "import sys,json; cs=json.load(sys.stdin); print('yes' if any(c.get('clientRole', False) and c.get('containerId')=='${client_uuid}' and c.get('name')=='${client_role}' for c in cs) else 'no')")

  if [[ "${has_child}" == "yes" ]]; then
    ok "Composite client '${realm_role}' contient deja '${client_id}:${client_role}'"
    return 0
  fi

  child_data=$(curl -sf \
    -H "Authorization: Bearer ${token}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/clients/${client_uuid}/roles/${client_role}" 2>/dev/null || echo "")
  [[ -n "${child_data}" ]] || { fail "Client role '${client_id}:${client_role}' introuvable"; return 1; }

  http=$(curl -so /dev/null -w "%{http_code}" \
    -X POST \
    -H "Authorization: Bearer ${token}" \
    -H "Content-Type: application/json" \
    -d "[${child_data}]" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles/${realm_role}/composites")

  if [[ "${http}" == "204" ]]; then
    ok "Composite client ajoute: '${client_id}:${client_role}' -> '${realm_role}'"
  else
    fail "Ajout composite client '${client_id}:${client_role}' -> '${realm_role}' echoue (HTTP ${http})"
    return 1
  fi
}

ensure_application_roles_baseline() {
  local token="$1"
  local clients=("sma-monolithe" "sma-thymeleaf-frontend")
  local client

  info "Verification baseline RBAC applicatif (roles client + composites)"

  for client in "${clients[@]}"; do
    ensure_client_role_exists "${token}" "${client}" "app-user" "Acces aux fonctionnalites standard"
    ensure_client_role_exists "${token}" "${client}" "app-admin" "Administration applicative"
    ensure_client_role_exists "${token}" "${client}" "app-auditor" "Audit et conformite en lecture"
    ensure_client_role_exists "${token}" "${client}" "manage-users" "Gestion des utilisateurs applicatifs"
    ensure_client_role_exists "${token}" "${client}" "manage-settings" "Gestion de la configuration applicative"
    ensure_client_role_exists "${token}" "${client}" "manage-reports" "Gestion des rapports applicatifs"
    ensure_client_role_exists "${token}" "${client}" "audit-read" "Lecture des traces d'audit"
    ensure_client_role_exists "${token}" "${client}" "audit-export" "Export des traces d'audit"
    ensure_client_role_exists "${token}" "${client}" "audit-analyze" "Analyse des donnees d'audit"
    ensure_client_role_exists "${token}" "${client}" "audit-verify" "Verification conformite/audit"

    ensure_realm_has_client_role "${token}" "USER" "${client}" "app-user"

    ensure_realm_has_client_role "${token}" "ADMIN" "${client}" "app-admin"
    ensure_realm_has_client_role "${token}" "ADMIN" "${client}" "manage-users"
    ensure_realm_has_client_role "${token}" "ADMIN" "${client}" "manage-settings"
    ensure_realm_has_client_role "${token}" "ADMIN" "${client}" "manage-reports"

    ensure_realm_has_client_role "${token}" "AUDITOR" "${client}" "app-auditor"
    ensure_realm_has_client_role "${token}" "AUDITOR" "${client}" "audit-read"
    ensure_realm_has_client_role "${token}" "AUDITOR" "${client}" "audit-export"
    ensure_realm_has_client_role "${token}" "AUDITOR" "${client}" "audit-analyze"
    ensure_realm_has_client_role "${token}" "AUDITOR" "${client}" "audit-verify"
  done

  # ADMIN herite de USER pour inclure les droits standards.
  ensure_realm_composite_role "${token}" "ADMIN" "USER"
}

ensure_self_registration_enabled() {
  local token="$1"
  local http

  http=$(curl -so /tmp/sma-realm-update.json -w "%{http_code}" \
    -X PUT \
    -H "Authorization: Bearer ${token}" \
    -H "Content-Type: application/json" \
    -d '{"registrationAllowed":true}' \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}")

  if [[ "${http}" == "204" ]]; then
    ok "Inscription utilisateur activee (registrationAllowed=true)"
  else
    warn "Impossible d'activer registrationAllowed (HTTP ${http})"
    cat /tmp/sma-realm-update.json 2>/dev/null || true
  fi
}

ensure_default_role_includes_user() {
  local token="$1"
  local default_role_name default_role_id user_role_data has_user http

  default_role_name=$(curl -sf \
    -H "Authorization: Bearer ${token}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}" \
    | python3 -c "import sys,json; r=json.load(sys.stdin); d=r.get('defaultRole') or {}; print(d.get('name',''))")

  if [[ -z "${default_role_name}" ]]; then
    fail "Impossible de determiner le role par defaut du realm"
    return 1
  fi

  default_role_id=$(curl -sf \
    -H "Authorization: Bearer ${token}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles/${default_role_name}" \
    | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))")

  [[ -n "${default_role_id}" ]] || { fail "Role par defaut introuvable: ${default_role_name}"; return 1; }

  has_user=$(curl -sf \
    -H "Authorization: Bearer ${token}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles/${default_role_name}/composites" \
    | python3 -c "import sys,json; cs=json.load(sys.stdin); print('yes' if any(c.get('name')=='USER' for c in cs) else 'no')")

  if [[ "${has_user}" == "yes" ]]; then
    ok "Le role par defaut '${default_role_name}' contient deja USER"
    return 0
  fi

  user_role_data=$(curl -sf \
    -H "Authorization: Bearer ${token}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles/USER" 2>/dev/null || echo "")
  [[ -n "${user_role_data}" ]] || { fail "Role USER introuvable"; return 1; }

  http=$(curl -so /dev/null -w "%{http_code}" \
    -X POST \
    -H "Authorization: Bearer ${token}" \
    -H "Content-Type: application/json" \
    -d "[${user_role_data}]" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles-by-id/${default_role_id}/composites")

  if [[ "${http}" == "204" ]]; then
    ok "Role USER ajoute au role par defaut '${default_role_name}'"
  else
    fail "Ajout USER au role par defaut echoue (HTTP ${http})"
    return 1
  fi
}

ensure_realm_baseline() {
  local token="$1"

  info "Verification baseline realm (roles + defaults)"
  ensure_role_exists "${token}" "ADMIN" "Administrateur applicatif"
  ensure_role_exists "${token}" "USER" "Utilisateur applicatif"
  ensure_role_exists "${token}" "AUDITOR" "Auditeur lecture seule"
  ensure_application_roles_baseline "${token}"
  ensure_self_registration_enabled "${token}"
  ensure_default_role_includes_user "${token}"
}

# -------------------------------------------------------------------------
# status : verifier que le realm repond
# -------------------------------------------------------------------------
cmd_status() {
  echo ""
  info "=== Statut Keycloak / Realm ${REALM} ==="

  # Keycloak alive ? (compatible images ou /health/* peut etre desactive)
  local keycloak_http="000"
  for probe in "/health/ready" "/realms/master/.well-known/openid-configuration" "/realms/${REALM}"; do
    keycloak_http=$(curl -so /dev/null -w "%{http_code}" "${KEYCLOAK_BASE_URL}${probe}" 2>/dev/null || echo "000")
    if [[ "$keycloak_http" == "200" ]]; then
      break
    fi
  done

  if [[ "$keycloak_http" == "200" ]]; then
    ok "Keycloak repond sur ${KEYCLOAK_BASE_URL}"
  else
    fail "Keycloak inaccessible (HTTP ${keycloak_http}) — verifiez que Docker est lance"
    echo "  docker compose up -d"
    exit 1
  fi

  # Token admin ?
  TOKEN=$(admin_token 2>/dev/null) || { fail "Impossible d'obtenir un token admin (mauvais mot de passe ?)"; exit 1; }
  ok "Token admin obtenu"

  # Realm existe ?
  HTTP=$(curl -so /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer ${TOKEN}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}" 2>/dev/null || echo "000")
  if [[ "$HTTP" == "200" ]]; then
    ok "Realm '${REALM}' present en base"
  else
    fail "Realm '${REALM}' introuvable (HTTP ${HTTP})"
    warn "Lancez : $0 restore"
    exit 1
  fi

  # Roles
  ROLES=$(curl -sf \
    -H "Authorization: Bearer ${TOKEN}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles" \
    | python3 -c "import sys,json; [print(' -', r['name']) for r in json.load(sys.stdin)]")
  info "Roles du realm :"
  echo "$ROLES"

  # Nombre d'utilisateurs
  COUNT=$(curl -sf \
    -H "Authorization: Bearer ${TOKEN}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/users/count" 2>/dev/null || echo "?")
  info "Nombre d'utilisateurs : ${COUNT}"

  echo ""
  ok "Le realm est operationnel. Il persistera tant que le volume 'pg_keycloak' existe."
  info "Pour sauvegarder : $0 backup"
}

# -------------------------------------------------------------------------
# backup : exporter le realm depuis Keycloak vers realm-export.json
# -------------------------------------------------------------------------
cmd_backup() {
  echo ""
  info "=== Export du realm '${REALM}' vers ${REALM_JSON} ==="

  TOKEN=$(admin_token 2>/dev/null) || { fail "Token admin impossible"; exit 1; }

  # L'API /partial-export exporte roles + clients + groupes
  HTTP=$(curl -so /tmp/sma-realm-backup.json -w "%{http_code}" \
    -X POST \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/partial-export?exportClients=true&exportGroupsAndRoles=true")

  if [[ "$HTTP" == "200" ]]; then
    cp /tmp/sma-realm-backup.json "${REALM_JSON}"
    ok "realm-export.json mis a jour depuis Keycloak"
    info "Commitez ce fichier pour ne jamais perdre votre realm :"
    echo "  git add docker/realm-export.json && git commit -m 'chore: backup realm Keycloak'"
  else
    fail "Export echoue (HTTP ${HTTP})"
    cat /tmp/sma-realm-backup.json 2>/dev/null || true
    exit 1
  fi
}

# -------------------------------------------------------------------------
# restore : forcer la reimportation du realm depuis le JSON local
# (utile si le volume a ete supprime ou sur une nouvelle machine)
# -------------------------------------------------------------------------
cmd_restore() {
  echo ""
  info "=== Reimportation du realm depuis ${REALM_JSON} ==="
  warn "Cette operation recrée le realm si absent, ou l'ignore s'il existe deja."

  TOKEN=$(admin_token 2>/dev/null) || { fail "Token admin impossible"; exit 1; }

  # Verifier si realm existe deja
  HTTP_CHECK=$(curl -so /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer ${TOKEN}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}" 2>/dev/null || echo "000")

  if [[ "$HTTP_CHECK" == "200" ]]; then
    warn "Le realm '${REALM}' existe deja — aucune action necessaire."
    info "Pour le reecraser completement, supprimez d'abord le volume :"
    echo "  docker compose down -v"
    echo "  docker compose up -d"
    return
  fi

  # Import
  HTTP=$(curl -so /dev/null -w "%{http_code}" \
    -X POST \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d @"${REALM_JSON}" \
    "${KEYCLOAK_BASE_URL}/admin/realms")

  if [[ "$HTTP" == "201" ]]; then
    ok "Realm '${REALM}' cree avec succes depuis le JSON"
  else
    fail "Import echoue (HTTP ${HTTP})"
    exit 1
  fi
}

# -------------------------------------------------------------------------
# create-user : creer un utilisateur dans le realm
# -------------------------------------------------------------------------
cmd_create_user() {
  echo ""
  info "=== Creation d'un utilisateur dans le realm '${REALM}' ==="

  read -rp "  Nom d'utilisateur   : " USERNAME
  read -rp "  Email               : " EMAIL
  read -rsp "  Mot de passe        : " PASSWORD; echo
  read -rp "  Role (ADMIN/USER/AUDITOR) : " ROLE

  TOKEN=$(admin_token 2>/dev/null) || { fail "Token admin impossible"; exit 1; }

  # Creer l'utilisateur
  HTTP=$(curl -so /dev/null -w "%{http_code}" \
    -X POST \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"${USERNAME}\",\"email\":\"${EMAIL}\",\"enabled\":true,\"credentials\":[{\"type\":\"password\",\"value\":\"${PASSWORD}\",\"temporary\":false}]}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/users")

  if [[ "$HTTP" != "201" ]]; then
    fail "Creation utilisateur echouee (HTTP ${HTTP})"
    exit 1
  fi
  ok "Utilisateur '${USERNAME}' cree"

  # Recuperer l'ID utilisateur
  USER_ID=$(curl -sf \
    -H "Authorization: Bearer ${TOKEN}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/users?username=${USERNAME}" \
    | python3 -c "import sys,json; users=json.load(sys.stdin); print(users[0]['id']) if users else print('')")

  if [[ -z "$USER_ID" ]]; then
    fail "Impossible de recuperer l'ID de l'utilisateur"
    exit 1
  fi

  # Recuperer l'ID du role
  ROLE_ID=$(curl -sf \
    -H "Authorization: Bearer ${TOKEN}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles/${ROLE}" \
    | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "")

  if [[ -z "$ROLE_ID" ]]; then
    warn "Role '${ROLE}' introuvable — utilisateur cree sans role"
  else
    # Assigner le role
    HTTP=$(curl -so /dev/null -w "%{http_code}" \
      -X POST \
      -H "Authorization: Bearer ${TOKEN}" \
      -H "Content-Type: application/json" \
      -d "[{\"id\":\"${ROLE_ID}\",\"name\":\"${ROLE}\"}]" \
      "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/users/${USER_ID}/role-mappings/realm")
    if [[ "$HTTP" == "204" ]]; then
      ok "Role '${ROLE}' assigne a '${USERNAME}'"
    else
      warn "Assignation du role echouee (HTTP ${HTTP})"
    fi
  fi
}

# -------------------------------------------------------------------------
# list-users : lister les utilisateurs
# -------------------------------------------------------------------------
cmd_list_users() {
  echo ""
  info "=== Utilisateurs du realm '${REALM}' ==="
  TOKEN=$(admin_token 2>/dev/null) || { fail "Token admin impossible"; exit 1; }

  curl -sf \
    -H "Authorization: Bearer ${TOKEN}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/users?max=100" \
    | python3 -c "
import sys, json
users = json.load(sys.stdin)
if not users:
    print('  (aucun utilisateur)')
else:
    print(f'  {len(users)} utilisateur(s) :')
    for u in users:
        print(f'  - {u[\"username\"]:20s}  {u.get(\"email\",\"\"):30s}  enabled={u[\"enabled\"]}')
"
}

# -------------------------------------------------------------------------
# add-role : assigner un role a un utilisateur existant
# -------------------------------------------------------------------------
cmd_add_role() {
  echo ""
  info "=== Assignation d'un role ==="
  read -rp "  Nom d'utilisateur : " USERNAME
  read -rp "  Role (ADMIN/USER/AUDITOR) : " ROLE

  TOKEN=$(admin_token 2>/dev/null) || { fail "Token admin impossible"; exit 1; }

  USER_ID=$(curl -sf \
    -H "Authorization: Bearer ${TOKEN}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/users?username=${USERNAME}" \
    | python3 -c "import sys,json; u=json.load(sys.stdin); print(u[0]['id']) if u else print('')")
  [[ -z "$USER_ID" ]] && { fail "Utilisateur '${USERNAME}' introuvable"; exit 1; }

  ROLE_DATA=$(curl -sf \
    -H "Authorization: Bearer ${TOKEN}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles/${ROLE}" 2>/dev/null || echo "")
  [[ -z "$ROLE_DATA" ]] && { fail "Role '${ROLE}' introuvable"; exit 1; }

  HTTP=$(curl -so /dev/null -w "%{http_code}" \
    -X POST \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d "[${ROLE_DATA}]" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/users/${USER_ID}/role-mappings/realm")

  [[ "$HTTP" == "204" ]] && ok "Role '${ROLE}' assigne a '${USERNAME}'" || fail "Echec (HTTP ${HTTP})"
}

# -------------------------------------------------------------------------
# upsert_user_with_role : cree un utilisateur si absent et assigne le role
# -------------------------------------------------------------------------
upsert_user_with_role() {
  local username="$1"
  local email="$2"
  local password="$3"
  local role="$4"
  local token="$5"

  local user_id role_data http
  user_id=$(curl -sf \
    -H "Authorization: Bearer ${token}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/users?username=${username}" \
    | python3 -c "import sys,json; u=json.load(sys.stdin); print(u[0]['id']) if u else print('')")

  if [[ -z "${user_id}" ]]; then
    http=$(curl -so /dev/null -w "%{http_code}" \
      -X POST \
      -H "Authorization: Bearer ${token}" \
      -H "Content-Type: application/json" \
      -d "{\"username\":\"${username}\",\"email\":\"${email}\",\"enabled\":true,\"credentials\":[{\"type\":\"password\",\"value\":\"${password}\",\"temporary\":false}]}" \
      "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/users")
    [[ "${http}" == "201" ]] || { fail "Creation de ${username} echouee (HTTP ${http})"; return 1; }

    user_id=$(curl -sf \
      -H "Authorization: Bearer ${token}" \
      "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/users?username=${username}" \
      | python3 -c "import sys,json; u=json.load(sys.stdin); print(u[0]['id']) if u else print('')")
    [[ -n "${user_id}" ]] || { fail "Impossible de retrouver ${username} apres creation"; return 1; }
    ok "Utilisateur '${username}' cree"
  else
    ok "Utilisateur '${username}' deja present"
  fi

  role_data=$(curl -sf \
    -H "Authorization: Bearer ${token}" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles/${role}" 2>/dev/null || echo "")
  [[ -n "${role_data}" ]] || { fail "Role '${role}' introuvable"; return 1; }

  http=$(curl -so /dev/null -w "%{http_code}" \
    -X POST \
    -H "Authorization: Bearer ${token}" \
    -H "Content-Type: application/json" \
    -d "[${role_data}]" \
    "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/users/${user_id}/role-mappings/realm")
  [[ "${http}" == "204" ]] || { fail "Assignation role ${role} -> ${username} echouee (HTTP ${http})"; return 1; }
  ok "Role '${role}' assigne a '${username}'"
}

# -------------------------------------------------------------------------
# seed-demo-users : cree 3 comptes de demo pour tests manuels
# -------------------------------------------------------------------------
cmd_seed_demo_users() {
  echo ""
  info "=== Seed des comptes de demo (${REALM}) ==="

  local token
  token=$(admin_token 2>/dev/null) || { fail "Token admin impossible"; exit 1; }

  ensure_realm_baseline "${token}"

  # Credentials par defaut, surchargables via variables d'environnement
  local admin_user="${KC_DEMO_ADMIN_USER:-admin-test}"
  local admin_email="${KC_DEMO_ADMIN_EMAIL:-admin-test@sma.local}"
  local admin_pwd="${KC_DEMO_ADMIN_PASSWORD:-admin123!}"

  local user_user="${KC_DEMO_USER_USER:-user-test}"
  local user_email="${KC_DEMO_USER_EMAIL:-user-test@sma.local}"
  local user_pwd="${KC_DEMO_USER_PASSWORD:-user123!}"

  local auditor_user="${KC_DEMO_AUDITOR_USER:-audit-test}"
  local auditor_email="${KC_DEMO_AUDITOR_EMAIL:-audit-test@sma.local}"
  local auditor_pwd="${KC_DEMO_AUDITOR_PASSWORD:-audit123!}"

  upsert_user_with_role "${admin_user}" "${admin_email}" "${admin_pwd}" "ADMIN" "${token}"
  upsert_user_with_role "${user_user}" "${user_email}" "${user_pwd}" "USER" "${token}"
  upsert_user_with_role "${auditor_user}" "${auditor_email}" "${auditor_pwd}" "AUDITOR" "${token}"

  echo ""
  ok "Comptes de demo prets :"
  echo "  ADMIN   -> ${admin_user} / ${admin_pwd}"
  echo "  USER    -> ${user_user} / ${user_pwd}"
  echo "  AUDITOR -> ${auditor_user} / ${auditor_pwd}"
  warn "Changez ces mots de passe en environnement non-local."
}

# -------------------------------------------------------------------------
# Main
# -------------------------------------------------------------------------
CMD="${1:-help}"
case "$CMD" in
  status)      cmd_status ;;
  backup)      cmd_backup ;;
  restore)     cmd_restore ;;
  create-user) cmd_create_user ;;
  list-users)  cmd_list_users ;;
  add-role)    cmd_add_role ;;
  seed-demo-users) cmd_seed_demo_users ;;
  *)
    echo ""
    echo "Usage: $0 {status|backup|restore|create-user|list-users|add-role|seed-demo-users}"
    echo ""
    echo "  status      — Verifie que Keycloak et le realm sont operationnels"
    echo "  backup      — Exporte le realm depuis Keycloak vers docker/realm-export.json"
    echo "  restore     — Importe le realm depuis docker/realm-export.json (si absent en base)"
    echo "  create-user — Cree un utilisateur avec son role"
    echo "  list-users  — Liste les utilisateurs du realm"
    echo "  add-role    — Assigne un role a un utilisateur existant"
    echo "  seed-demo-users — Cree/maj 3 comptes de demo (ADMIN/USER/AUDITOR)"
    echo ""
    echo "Variables d'environnement (optionnelles) :"
    echo "  KEYCLOAK_BASE_URL      (defaut: http://localhost:8080)"
    echo "  KEYCLOAK_ADMIN         (defaut: admin)"
    echo "  KEYCLOAK_ADMIN_PASSWORD (defaut: admin)"
    echo "  KEYCLOAK_REALM         (defaut: sma-realm)"
    echo ""
    ;;
esac

