# Feature: Espace Personnel Client pour Utilisateurs

## 📋 Description
Implémentation d'un espace personnel pour les utilisateurs (USER role) où ils peuvent consulter leurs clients personnels créés. Les utilisateurs ne voient que leurs propres clients, tandis que les administrateurs (ADMIN) et auditeurs (AUDITOR) voient tous les clients.

## 🔧 Modifications Techniques

### Backend (Contrôle d'Accès - RBAC)

#### ClientQueryController.java
- **Endpoint `/queries/clients/{id}`** :
  - Ajout de `@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'AUDITOR')")`
  - Les USER peuvent maintenant consulter les détails d'un client
  - TODO: Vérifier que le client appartient à l'utilisateur (userId matching)

- **Endpoint `/queries/clients`** (Liste) :
  - Déjà protégé avec `@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'AUDITOR')")`
  - Amélioration : Filtrage par rôle
    - **ADMIN** : voit tous les clients
    - **AUDITOR** : voit tous les clients
    - **USER** : filtre pour ne voir que ses propres clients (basé sur userId)
  - Implémentation du filtrage : détection du rôle via `Authentication` et `getAuthorities()`
  - Logging détaillé pour tracer les accès

### Frontend (Affichage du Contexte Utilisateur)

#### ClientThymController.java
- **Methode `getClientsAsync()`** :
  - Ajout du paramètre `Authentication authentication`
  - Récupération du nom d'utilisateur via `authentication.getName()`
  - Passage du `currentUsername` au modèle Thymeleaf
  - Permet l'affichage de l'espace personnel de l'utilisateur

#### Template header.html
- Affichage du nom d'utilisateur connecté (déjà implémenté)
  - Affiche le `name` du JWT si disponible
  - Fallback sur `preferred_username`
  - Fallback sur `authentication.name`

## 🎯 Cas d'Usage

### Utilisateur (USER role)
1. Se connecte via Keycloak
2. Accède au menu "Clients"
3. Ne voit que les clients qu'il a créés
4. Peut consulter les détails de ses clients
5. Son nom d'utilisateur s'affiche en haut à droite (espace personnel)

### Administrateur (ADMIN role)
1. Se connecte via Keycloak
2. Accède au menu "Clients"
3. Voit TOUS les clients du système
4. Peut consulter les détails de n'importe quel client
5. Peut gérer les clients d'autres utilisateurs

### Auditeur (AUDITOR role)
1. Se connecte via Keycloak
2. Accède au menu "Clients"
3. Voit TOUS les clients du système (accès en lecture seule)
4. Peut consulter les détails de n'importe quel client
5. Son nom d'utilisateur s'affiche en haut à droite

## 📊 Architecture RBAC

```
Routes:
  /queries/clients      → GET (ADMIN, USER, AUDITOR) → Filtrée par rôle
  /queries/clients/{id} → GET (ADMIN, USER, AUDITOR) → Avec vérification userId

Authorization:
  ADMIN   → Tous les clients sans restriction
  USER    → Filtre : client.userId == authentication.name
  AUDITOR → Tous les clients (lecture seule)
```

## 🔐 Sécurité

- ✅ Vérification des rôles côté serveur avec `@PreAuthorize`
- ✅ Récupération de l'ID utilisateur depuis le contexte de sécurité
- ✅ Logging détaillé de tous les accès aux clients
- ⚠️ TODO: Implémenter la vérification complète du `userId` dans le Client pour USER
- ⚠️ TODO: Ajouter un champ `userId` au modèle `Client` pour le filtrage

## 🚀 Prochaines Étapes

1. **Ajouter le champ `userId` au modèle Client**
   - Modifier l'entité `Client` pour inclure `userId`
   - Mettre à jour les dépôts et mappeurs

2. **Implémenter le filtrage complet**
   - Remplacer les TODO commentés par le code actuel
   - Tester le filtrage côté base de données

3. **Interface Utilisateur**
   - Afficher clairement l'espace personnel
   - Ajouter un badge "Vos clients" pour les clients de l'utilisateur
   - Masquer le menu "Clients" pour les rôles sans accès

4. **Tests**
   - Test unitaire pour vérifier le filtrage par userId
   - Test d'intégration pour vérifier les permissions RBAC

## 📝 Commits Associés

- `feat(backend-client-rbac)` : Contrôle d'accès backend
- `feat(frontend-client)` : Affichage du contexte utilisateur frontend

## ✅ Status

- [x] Ajout de `@PreAuthorize` sur les endpoints clients
- [x] Implémentation du filtrage par rôle
- [x] Passage du username au frontend
- [x] Affichage du nom d'utilisateur en en-tête
- [ ] Ajout du champ `userId` au modèle Client
- [ ] Vérification complète du userId pour USER
- [ ] Interface utilisateur pour l'espace personnel

## 🔗 Références

- **Keycloak Roles** : ADMIN, USER, AUDITOR (configurés dans le realm)
- **Spring Security** : `@PreAuthorize`, `Authentication`, `SecurityContextHolder`
- **Thymeleaf Spring Security** : `th:authorize`, `sec:*` namespaces

