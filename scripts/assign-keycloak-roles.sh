#!/bin/bash

# Script pour assigner les rôles aux utilisateurs dans Keycloak
# À exécuter après que Keycloak soit en cours d'exécution

KEYCLOAK_URL="http://localhost:8080"
REALM="sma-realm"
ADMIN_USER="admin"
ADMIN_PASSWORD="admin"

# Obtenir le token d'authentification
echo "Obtention du token d'authentification..."
TOKEN=$(curl -s -X POST \
  "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=admin-cli" \
  -d "username=${ADMIN_USER}" \
  -d "password=${ADMIN_PASSWORD}" \
  -d "grant_type=password" | jq -r '.access_token')

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
  echo "❌ Erreur : Impossible d'obtenir le token. Vérifiez les identifiants."
  exit 1
fi

echo "✅ Token obtenu avec succès"

# Obtenir l'ID du realm
echo "Obtention de l'ID du realm..."
REALM_ID=$(curl -s -X GET \
  "${KEYCLOAK_URL}/admin/realms/${REALM}" \
  -H "Authorization: Bearer ${TOKEN}" | jq -r '.id')

if [ -z "$REALM_ID" ] || [ "$REALM_ID" == "null" ]; then
  echo "❌ Erreur : Impossible de trouver le realm '${REALM}'"
  exit 1
fi

echo "✅ Realm ID: ${REALM_ID}"

# Obtenir les IDs de rôle
echo "Obtention des IDs de rôle..."
ADMIN_ROLE_ID=$(curl -s -X GET \
  "${KEYCLOAK_URL}/admin/realms/${REALM}/roles" \
  -H "Authorization: Bearer ${TOKEN}" | jq -r '.[] | select(.name=="ADMIN") | .id')

USER_ROLE_ID=$(curl -s -X GET \
  "${KEYCLOAK_URL}/admin/realms/${REALM}/roles" \
  -H "Authorization: Bearer ${TOKEN}" | jq -r '.[] | select(.name=="USER") | .id')

AUDITOR_ROLE_ID=$(curl -s -X GET \
  "${KEYCLOAK_URL}/admin/realms/${REALM}/roles" \
  -H "Authorization: Bearer ${TOKEN}" | jq -r '.[] | select(.name=="AUDITOR") | .id')

echo "✅ Rôles trouvés:"
echo "   ADMIN: ${ADMIN_ROLE_ID}"
echo "   USER: ${USER_ROLE_ID}"
echo "   AUDITOR: ${AUDITOR_ROLE_ID}"

# Obtenir l'ID de l'utilisateur admin-test
echo ""
echo "Obtention de l'ID de l'utilisateur admin-test..."
ADMIN_TEST_ID=$(curl -s -X GET \
  "${KEYCLOAK_URL}/admin/realms/${REALM}/users?username=admin-test" \
  -H "Authorization: Bearer ${TOKEN}" | jq -r '.[0].id')

if [ -z "$ADMIN_TEST_ID" ] || [ "$ADMIN_TEST_ID" == "null" ]; then
  echo "❌ Erreur : Utilisateur 'admin-test' non trouvé"
else
  echo "✅ admin-test ID: ${ADMIN_TEST_ID}"

  # Assigner le rôle ADMIN à admin-test
  echo "Attribution du rôle ADMIN à admin-test..."
  curl -s -X POST \
    "${KEYCLOAK_URL}/admin/realms/${REALM}/users/${ADMIN_TEST_ID}/role-mappings/realm" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d "[{\"id\":\"${ADMIN_ROLE_ID}\",\"name\":\"ADMIN\"}]"
  echo "✅ Rôle ADMIN assigné"
fi

# Obtenir l'ID de l'utilisateur user-test
echo ""
echo "Obtention de l'ID de l'utilisateur user-test..."
USER_TEST_ID=$(curl -s -X GET \
  "${KEYCLOAK_URL}/admin/realms/${REALM}/users?username=user-test" \
  -H "Authorization: Bearer ${TOKEN}" | jq -r '.[0].id')

if [ -z "$USER_TEST_ID" ] || [ "$USER_TEST_ID" == "null" ]; then
  echo "❌ Erreur : Utilisateur 'user-test' non trouvé"
else
  echo "✅ user-test ID: ${USER_TEST_ID}"

  # Assigner le rôle USER à user-test
  echo "Attribution du rôle USER à user-test..."
  curl -s -X POST \
    "${KEYCLOAK_URL}/admin/realms/${REALM}/users/${USER_TEST_ID}/role-mappings/realm" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d "[{\"id\":\"${USER_ROLE_ID}\",\"name\":\"USER\"}]"
  echo "✅ Rôle USER assigné"
fi

# Obtenir l'ID de l'utilisateur auditor-test
echo ""
echo "Obtention de l'ID de l'utilisateur auditor-test..."
AUDITOR_TEST_ID=$(curl -s -X GET \
  "${KEYCLOAK_URL}/admin/realms/${REALM}/users?username=auditor-test" \
  -H "Authorization: Bearer ${TOKEN}" | jq -r '.[0].id')

if [ -z "$AUDITOR_TEST_ID" ] || [ "$AUDITOR_TEST_ID" == "null" ]; then
  echo "❌ Erreur : Utilisateur 'auditor-test' non trouvé"
else
  echo "✅ auditor-test ID: ${AUDITOR_TEST_ID}"

  # Assigner le rôle AUDITOR à auditor-test
  echo "Attribution du rôle AUDITOR à auditor-test..."
  curl -s -X POST \
    "${KEYCLOAK_URL}/admin/realms/${REALM}/users/${AUDITOR_TEST_ID}/role-mappings/realm" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d "[{\"id\":\"${AUDITOR_ROLE_ID}\",\"name\":\"AUDITOR\"}]"
  echo "✅ Rôle AUDITOR assigné"
fi

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║  ✅ Attribution des rôles Keycloak complétée !           ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "Utilisateurs configurés:"
echo "  • admin-test   → ROLE_ADMIN"
echo "  • user-test    → ROLE_USER"
echo "  • auditor-test → ROLE_AUDITOR"

