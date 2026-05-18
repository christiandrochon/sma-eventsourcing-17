<!-- 
═════════════════════════════════════════════════════════════════════════════
📑 DOCS_STRUCTURE.md
═════════════════════════════════════════════════════════════════════════════
Qu'il contient : Explication de l'organisation de la documentation du projet
Utilité : Comprendre où lire quoi, et quel fichier ouvrir selon le besoin
Public : Tous (navigation de documentation)
À consulter : Quand vous êtes perdu dans la documentation
À archiver : Non (guide de navigation)
═════════════════════════════════════════════════════════════════════════════
-->
# 📑 Organisation de la Documentation
La documentation du projet est organisée autour d’un **point d’entrée unique** : [`README.docs.md`](./README.docs.md).
---
## 🌐 Point d’entrée recommandé
- **[`README.docs.md`](./README.docs.md)** → navigation principale du dossier `docs/`
- **[`../README.md`](../README.md)** → documentation principale du projet à la racine
---
## 📂 Catégories de documentation
### Stratégique
- [`LOGGING_STRATEGY.md`](./LOGGING_STRATEGY.md)
- [`FEATURE_USERID_DOSSIER.md`](./FEATURE_USERID_DOSSIER.md)
- [`FEATURE_USER_CLIENT_ACCESS.md`](./FEATURE_USER_CLIENT_ACCESS.md)
### Opérationnelle
- [`QUICK_START_LOGGING.md`](./QUICK_START_LOGGING.md)
- [`KEYCLOAK_ROLES_SETUP.md`](./KEYCLOAK_ROLES_SETUP.md)
### Historique
- [`CHANGELOG_LOGGING.md`](./CHANGELOG_LOGGING.md)
- [`MODIFIED_FILES.md`](./MODIFIED_FILES.md)
### Navigation
- [`README.docs.md`](./README.docs.md) — entrée principale des docs détaillées
---
## 🧭 Où aller selon le besoin ?
| Besoin | Fichier recommandé |
|---|---|
| Comprendre la documentation | [`README.docs.md`](./README.docs.md) |
| Comprendre l’architecture des logs | [`LOGGING_STRATEGY.md`](./LOGGING_STRATEGY.md) |
| Lire rapidement les logs | [`QUICK_START_LOGGING.md`](./QUICK_START_LOGGING.md) |
| Résoudre un problème Keycloak | [`KEYCLOAK_ROLES_SETUP.md`](./KEYCLOAK_ROLES_SETUP.md) |
| Lire une spec de feature | `FEATURE_*.md` |
| Voir l’historique d’un changement | [`CHANGELOG_LOGGING.md`](./CHANGELOG_LOGGING.md) |
| Voir l’impact d’un changement | [`MODIFIED_FILES.md`](./MODIFIED_FILES.md) |
---
## ✅ Règle simple
- **`README.md` à la racine** = documentation principale du projet
- **`docs/README.docs.md`** = entrée unique du dossier `docs/`
---
**Dernière mise à jour** : 2026-05-18  
**Auteur** : GitHub Copilot
