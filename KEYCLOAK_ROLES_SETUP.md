# Configuration des Rôles Keycloak - SOLUTION RAPIDE

## ⚠️ PROBLÈME

L'utilisateur `user-test` n'a **pas accès au menu Clients** car il n'a **pas le rôle USER assigné dans Keycloak**.

## ✅ SOLUTION - 2 APPROCHES

### Approche 1 : Automatique (Recommandée) - Script Bash

**Exécutez ce script une fois que Keycloak est en cours d'exécution :**

```bash
./scripts/assign-keycloak-roles.sh
```

Ce script va :
- Se connecter à Keycloak en tant qu'admin
- Récupérer les IDs des rôles (ADMIN, USER, AUDITOR)
- Assigner les rôles aux utilisateurs de test :
  - `admin-test` → `ROLE_ADMIN`
  - `user-test` → `ROLE_USER`
  - `auditor-test` → `ROLE_AUDITOR`

### Approche 2 : Manuel via l'interface Keycloak Admin

1. **Ouvrir Keycloak Admin Console**
   - URL: `http://localhost:8080/admin/master/console/`
   - Username: `admin`
   - Password: `admin`

2. **Sélectionner le realm `sma-realm`**
   - En haut à gauche, dropdown "Master" → "sma-realm"

3. **Accéder aux Utilisateurs**
   - Menu gauche → "Users"

4. **Pour chaque utilisateur :**

   #### `user-test`
   - Cliquer sur "user-test"
   - Onglet "Role mapping"
   - "Assign role"
   - Chercher "USER"
   - Sélectionner et cliquer "Assign"

   #### `admin-test`
   - Cliquer sur "admin-test"
   - Onglet "Role mapping"
   - "Assign role"
   - Chercher "ADMIN"
   - Sélectionner et cliquer "Assign"

   #### `auditor-test`
   - Cliquer sur "auditor-test"
   - Onglet "Role mapping"
   - "Assign role"
   - Chercher "AUDITOR"
   - Sélectionner et cliquer "Assign"

## 🔐 Comment Ça Marche

### Flux de Sécurité

```
1. User se connecte via Keycloak
   ↓
2. Keycloak génère un JWT avec les rôles dans "realm_access.roles"
   ↓
3. Spring Security reçoit le JWT
   ↓
4. JwtAuthConverter extrait les rôles de "realm_access.roles"
   ↓
5. Les rôles sont convertis en GrantedAuthority avec préfixe ROLE_
   ↓
6. @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'AUDITOR')") vérifie le rôle
   ↓
7. User a accès au menu Clients ✅
```

### Mapping des Rôles

| Keycloak Role | Spring Security Authority |
|---------------|---------------------------|
| ADMIN         | ROLE_ADMIN               |
| USER          | ROLE_USER                |
| AUDITOR       | ROLE_AUDITOR             |

## 🎯 Résultat Attendu

Après assignation des rôles, les utilisateurs auront accès :

- **user-test** : 
  - ✅ Menu "Clients" visible
  - ✅ Voir uniquement ses clients (ceux qu'il a créés)
  - ✅ Consulter les détails de ses clients
  - ✅ Affichage de "user-test" en en-tête

- **admin-test** :
  - ✅ Menu "Clients" visible
  - ✅ Voir TOUS les clients
  - ✅ Gérer les clients des autres utilisateurs
  - ✅ Affichage de "admin-test" en en-tête

- **auditor-test** :
  - ✅ Menu "Clients" visible
  - ✅ Voir TOUS les clients (lecture seule)
  - ✅ Consulter les détails de tous les clients
  - ✅ Affichage de "auditor-test" en en-tête

## 🔍 Vérification

Pour vérifier que les rôles ont bien été assignés :

```bash
# Dans Keycloak Admin Console :
1. Users → user-test
2. Onglet "Role mapping"
3. Voir la liste des rôles assignés (doit inclure "USER")

# Ou via API :
curl -X GET http://localhost:8080/admin/realms/sma-realm/users \
  -H "Authorization: Bearer <token>" | jq '.[] | {username, roles}'
```

## 🚀 Prochaines Étapes

1. **Exécuter le script** : `./scripts/assign-keycloak-roles.sh`
2. **Se déconnecter et reconnecter** avec l'utilisateur `user-test`
3. **Vérifier que le menu Clients apparaît** ✅
4. **Tester la création de clients** et vérifier le filtrage par userId

## 📝 Configuration Actuelle (Backend)

- **ClientQueryController.java** : 
  - `@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'AUDITOR')")` sur `/queries/clients` et `/queries/clients/{id}`
  - Filtrage par `userId` pour les USER

- **JwtAuthConverter.java** :
  - Extrait les rôles de `realm_access.roles`
  - Ajoute le préfixe `ROLE_` automatiquement

## ⚡ Troubleshooting

### "user-test" n'a toujours pas accès au menu ?

1. **Vérifier que le rôle est bien assigné** :
   - Keycloak Admin Console → Users → user-test → Role mapping
   - USER doit être dans la liste des rôles assignés

2. **Vérifier le JWT** :
   - Ouvrir DevTools (F12) → Network
   - Se connecter
   - Chercher les requêtes Keycloak
   - Vérifier le JWT contient `"realm_access": {"roles": ["USER"]}`

3. **Redémarrer l'application** si nécessaire

4. **Vérifier les logs** :
   - Backend doit afficher : `BIZ_CLIENT_LIST_REQUEST`
   - Si 403 Forbidden : le rôle n'est pas dans le JWT

## 💡 C'EST TOUT !

Les rôles Keycloak ne s'assignent **que dans Keycloak**. 
Spring Security lit juste les rôles du JWT que Keycloak génère.

**Il n'y a RIEN à faire côté Spring** - la configuration est déjà en place ! 
La solution était juste d'assigner les rôles dans Keycloak.

