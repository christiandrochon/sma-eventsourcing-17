<!-- 
═════════════════════════════════════════════════════════════════════════════
📚 docs/README.docs.md - Entrée unique de la documentation
═════════════════════════════════════════════════════════════════════════════
Ce dossier contient : Toute la documentation détaillée du projet (architecture, guides, specs)
Utilité : Point d'entrée unique pour parcourir la documentation du projet
Public : Tous (dev, QA, DevOps, PM, auditeurs)
À consulter : Quand vous cherchez une info spécifique sur le projet
À archiver : Non (documentation vivante)
═════════════════════════════════════════════════════════════════════════════
-->
# 📚 Documentation du Projet SMA Event Sourcing
Bienvenue dans la documentation détaillée du projet SMA.
> **Point d’entrée unique** : ce fichier sert de navigation principale pour tout le dossier `docs/`.
> Le README principal du projet reste à la racine : [`../README.md`](../README.md).
---
## 📂 Fichiers disponibles
### Documentation stratégique
- [`LOGGING_STRATEGY.md`](./LOGGING_STRATEGY.md) — architecture des logs métier et techniques
- [`FEATURE_USERID_DOSSIER.md`](./FEATURE_USERID_DOSSIER.md) — spécification du traçage `userId` sur les dossiers
- [`FEATURE_USER_CLIENT_ACCESS.md`](./FEATURE_USER_CLIENT_ACCESS.md) — spécification RBAC sur l’accès aux clients
### Documentation opérationnelle
- [`QUICK_START_LOGGING.md`](./QUICK_START_LOGGING.md) — commandes rapides pour lire les logs
- [`KEYCLOAK_ROLES_SETUP.md`](./KEYCLOAK_ROLES_SETUP.md) — dépannage des rôles Keycloak
### Historique et suivi
- [`CHANGELOG_LOGGING.md`](./CHANGELOG_LOGGING.md) — rapport de mise à jour du logging métier
- [`MODIFIED_FILES.md`](./MODIFIED_FILES.md) — inventaire des fichiers modifiés pour le logging
### Navigation et structure
- [`DOCS_STRUCTURE.md`](./DOCS_STRUCTURE.md) — organisation globale de la documentation
---
## 🧭 Comment naviguer
- **Vous voulez comprendre l’organisation des docs ?** → [`DOCS_STRUCTURE.md`](./DOCS_STRUCTURE.md)
- **Vous cherchez une feature ?** → `FEATURE_*.md`
- **Vous voulez déboguer les logs ?** → [`QUICK_START_LOGGING.md`](./QUICK_START_LOGGING.md)
- **Vous devez résoudre un problème Keycloak ?** → [`KEYCLOAK_ROLES_SETUP.md`](./KEYCLOAK_ROLES_SETUP.md)
- **Vous voulez l’historique des changements logging ?** → [`CHANGELOG_LOGGING.md`](./CHANGELOG_LOGGING.md)
---
## 🗂️ Structure du dossier `docs/`
```text
docs/
├── README.docs.md
├── DOCS_STRUCTURE.md
├── LOGGING_STRATEGY.md
├── QUICK_START_LOGGING.md
├── CHANGELOG_LOGGING.md
├── MODIFIED_FILES.md
├── FEATURE_USERID_DOSSIER.md
├── FEATURE_USER_CLIENT_ACCESS.md
└── KEYCLOAK_ROLES_SETUP.md
```
---
## ✅ Règle simple
- **`README.md` à la racine** = documentation principale du projet
- **`docs/README.docs.md`** = entrée unique pour toute la documentation détaillée
---
**Dernière mise à jour** : 2026-05-18  
**Auteur** : GitHub Copilot
