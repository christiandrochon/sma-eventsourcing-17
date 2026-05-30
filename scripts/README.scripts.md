<!--
═════════════════════════════════════════════════════════════════════════════
📚 scripts/README.scripts.md
═════════════════════════════════════════════════════════════════════════════
Ce dossier contient : Les scripts d'automatisation, de démarrage et d'audit
Utilité : Point d'entrée unique pour comprendre quoi exécuter selon le besoin
Public : Tous (dev, QA, DevOps, auditeurs)
À consulter : Quand vous voulez lancer, diagnostiquer ou exporter l'audit
À archiver : Non (documentation vivante)
═════════════════════════════════════════════════════════════════════════════
-->
# Scripts utilitaires
## Guide rapide
| Je veux... | Commande | Description courte |
|---|---|---|
| **Démarrer l'appli** | `./scripts/dev.sh secure` | Lance tout avec Keycloak |
| **Démarrer pour debug** | `./scripts/dev.sh fast` | Lance tout sans login |
| **Voir les URLs et credentials** | `./scripts/dev.sh show` | Affiche comment se connecter |
| **Ouvrir l'appli directement** | `./scripts/dev.sh open` | Affiche URLs + ouvre navigateur |
| **Vérifier que tout répond** | `./scripts/dev.sh check` | Test HTTP rapide |
| **Créer les comptes de démo** | `./scripts/dev.sh seed-users` | admin-test, user-test, audit-test |
| **Arrêter la stack** | `./scripts/dev.sh down` | Arrête les conteneurs |
| **Charger l'audit en données** | `./scripts/audit-load-test.sh` | Génère ~180 événements audit |
| **Ajouter des verdicts audit** | `./scripts/audit-populate-checks.sh` | Crée 34 contrôles |
| **Vérifier l'état d'audit** | `./scripts/audit-check.sh` | Diagnostic de la base audit |
| **Exporter pour auditeur** | `./scripts/audit-export.sh` | Produit un dossier CSV |
| **Campagne audit complète** | `./scripts/audit-external-40.sh` | Valide tout pour audit externe |
| **Vérifier une PR Sonar** | `./scripts/sonar-pr-status.sh <project_key> <pr>` | Affiche Quality Gate + issues ouvertes |
---
## Principaux scripts
### `dev.sh`
Point d'entrée unique pour lancer la stack locale.
### `dev-up.sh`
Lance la stack Docker (PostgreSQL, Keycloak, Axon, Backend, Frontend).
### `dev-login.sh`
Affiche les URLs utiles et les credentials de démo.
### `keycloak-realm.sh`
Gère le realm Keycloak (users, rôles, export/import).
### `keycloak-token.sh`
Récupère un JWT sans login interactif.
### `assign-keycloak-roles.sh`
Assigne des rôles aux users Keycloak existants.
### `audit-load-test.sh`
Génère des événements d'audit via les endpoints applicatifs.
### `audit-populate-checks.sh`
Crée des verdicts de conformité dans la base audit.
### `audit-check.sh`
Diagnostique la santé de la base d'audit.
### `audit-export.sh`
Exporte les preuves d'audit vers des CSV.
### `audit-external-40.sh`
Lance la campagne complète d'audit externe en 40 cas.
### `sonar-pr-status.sh`
Vérifie le statut SonarCloud d'une PR (Quality Gate + issues ouvertes uniquement).
Permet d'éviter de confondre les issues historiques et les issues réellement ouvertes.
---
## Règle simple
- **`README.md` à la racine** = documentation principale du projet
- **`scripts/README.scripts.md`** = documentation des scripts
- **`docs/README.docs.md`** = documentation détaillée du projet
---
**Dernière mise à jour** : 2026-05-18
**Auteur** : GitHub Copilot
