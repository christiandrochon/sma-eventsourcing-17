<!-- 
═════════════════════════════════════════════════════════════════════════════
👤 FEATURE_USERID_DOSSIER.md
═════════════════════════════════════════════════════════════════════════════
Qu'il contient : Spécification feature pour tracer userId dans les dossiers
Utilité : Documentation de la capture automatique du créateur (userId) lors de création
Public : Développeurs, testeurs (documentation technique de feature)
À consulter : Avant de modifier la création de dossiers / traçabilité utilisateur
À archiver : Non (spécification active)
═════════════════════════════════════════════════════════════════════════════
-->

# Feature: Ajout du userId à la création de dossiers
## 📋 Description
Intégration complète du traçage de l'utilisateur pour chaque dossier créé. L'ID de l'utilisateur authentifié via OAuth2/Keycloak est automatiquement capturé et associé à chaque nouveau dossier.
## 🔧 Modifications Techniques
### Backend (CQRS + Event Sourcing)
- **DTO**: Ajout du champ `userId` à `DossierCommandDTO`
- **Commande**: Extension de `DossierCreateCommand` avec le paramètre `userId`
- **Événement**: Ajout du champ `userId` à `DossierCreatedEvent` pour l'event store
- **Agrégat**: Mise à jour de `DossierAggregate` pour gérer le `userId`
- **Service**: Restauration du système `CompletableFuture` dans `DossierCommandService`
  - Stockage des futures dans une `ConcurrentHashMap` indexée par dossier ID
  - Résolution de la future lors de la complétion de l'event handler
- **Tests**: Mise à jour de tous les fichiers de test avec la constante `USER_ID`
### Frontend (Spring Boot Thymeleaf + OAuth2)
- **DTOs**: 
  - Création du nouveau `UserThymDTO` pour représentation optionnelle
  - Ajout du `userId` à `DossierThymDTO` et `DossierThymConvertDTO`
- **Contrôleur**: `CreateDossierThymController`
  - Récupération de l'utilisateur via `Authentication` object
  - Pré-remplissage automatique du `userId` avec `authentication.getName()`
  - Passage du `userId` au backend lors de la conversion du DTO
- **Template**: Ajout d'un champ `<input type="hidden">` pour le userId
  - Pré-rempli automatiquement (aucune interaction utilisateur requise)
  - Totalement transparent pour l'utilisateur
## 🎯 Avantages
1. **Traçabilité**: Chaque dossier est lié à l'utilisateur qui l'a créé
2. **RGPD Compliant**: Possibilité de récupérer tous les dossiers d'un utilisateur
3. **Audit**: L'événement `DossierCreatedEvent` persiste le userId dans l'event store
4. **Sécurité**: Le userId est capturé côté serveur, impossible à contrefaire côté client
5. **Transparent**: Aucune modification de l'UX requise
## 📊 Commits
| # | Type | Description | Fichiers |
|---|------|-------------|----------|
| 1 | feat | Command backend | DossierCommandDTO, DossierCreateCommand |
| 2 | feat | Agrégat et Handler | DossierAggregate, DossierEventHandler |
| 3 | feat | Service avec CompletableFuture | DossierCommandService |
| 4 | test | Données de test | DossierTestDataFactory, Tests |
| 5 | feat | DTOs frontend | UserThymDTO, DossierThymDTO, DossierThymConvertDTO |
| 6 | feat | Contrôleur frontend | CreateDossierThymController |
| 7 | feat | Template HTML | createDossierForm.html |
| 8 | chore | Config et docs | application.properties, scripts/README.scripts.md |
| 9 | refactor | Autres templates | error.html, header.html, index.html |
## ✅ Status
- [x] Backend compile avec succès
- [x] Frontend compile avec succès
- [x] Tous les commits sont appliqués
- [x] Aucun conflit Git
- [x] Documentation complète
## 🚀 Prêt pour
- Tests d'intégration
- Déploiement en docker-compose
- Validation par l'utilisateur
