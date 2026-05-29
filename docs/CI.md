CI et SonarCloud — Guide rapide
================================

But: fournir une documentation concise pour les workflows GitHub Actions présents, expliquer comment lancer SonarCloud en local ou via CI, indiquer les changements que j'ai appliqués et quoi vérifier dans GitHub / Sonar.

1) Checklist rapide
- Ajouter le secret `SONAR_TOKEN` dans GitHub (Repository → Settings → Secrets) pour activer l'analyse SonarCloud depuis les workflows.
- Vérifier que les workflows dans `.github/workflows/` sont présents et contiennent une étape `mvn sonar:sonar` conditionnée par la présence du secret.
- Lancer localement : tests + analyse Sonar (si vous avez le token) en évitant la connexion à AxonServer : passez `-Daxon.axonserver.enabled=false`.

2) Fichiers CI (emplacements absolus et rôle)
- /home/cdn/IdeaProjects/sma-eventsourcing-17/.github/workflows/ci-build-and-openapi-check-sonarcloud.yml
  - Variante complète: build backend+frontend, vérifie OpenAPI et lance SonarCloud si les secrets sont présents. Utilisée pour validation complète et PR avec analyses.

- /home/cdn/IdeaProjects/sma-eventsourcing-17/.github/workflows/ci-build-and-openapi-check.yml
  - Build complet + vérification OpenAPI sans forcer SonarCloud. Utilisée si Sonar non configuré dans le repo (fallback).

- /home/cdn/IdeaProjects/sma-eventsourcing-17/.github/workflows/ci-build-only.yml
  - Job minimal qui compile le projet (backend + frontend) — rapide, utilisé comme check basique sur PRs pour valider la compilation.

- /home/cdn/IdeaProjects/sma-eventsourcing-17/.github/workflows/ci-frontend-tests.yml
  - Exécute uniquement les tests front-end (mvc/unit tests). Permet de séparer le temps d'exécution des tests front et back.

- /home/cdn/IdeaProjects/sma-eventsourcing-17/.github/workflows/ci-sanity.yml
  - Petit job de sanity: vérifie que l'environnement runner et les scripts sont ok (création de répertoires, permissions…).
+
+3) SonarCloud / SonarQube — mode opératoire
+
+Pour SonarCloud (cloud) : créez un token dans SonarCloud (Account → Security) puis stockez-le dans GitHub Secrets comme `SONAR_TOKEN`.
+Le workflow Sonar du repo est conditionnel: s'il trouve `SONAR_TOKEN` il exécute `mvn sonar:sonar` et pousse le résultat vers SonarCloud.
+Lancement local (exemple) sans exposer le token dans l'historique shell :
+
+```bash
+cd /home/cdn/IdeaProjects/sma-eventsourcing-17/backend
+read -s SONAR_TOKEN
+export SONAR_TOKEN
+mvn clean verify -Daxon.axonserver.enabled=false \
+  -Dsonar.host.url="https://sonarcloud.io" \
+  -Dsonar.login="$SONAR_TOKEN" \
+  sonar:sonar
+```
+
+4) Commandes utiles (CI / debug)
+
+Forcer un rerun des workflows (rapide): pousser un commit vide
+
+```bash
+git checkout -b docs/ci-add
+git add docs/CI.md
+git commit -m "docs(ci): add CI & SonarCloud documentation"
+git push -u origin docs/ci-add
+# puis créer la PR (si gh CLI est configuré)
+gh pr create --title "docs(ci): add CI & Sonar documentation" --body "Ajoute docs/CI.md : description des workflows et procédure SonarCloud." --base main
+```
+
+Alternative pour relancer CI sans PR: dans GitHub Actions → choisir le run → "Re-run jobs". Avec `gh` :
+  - lister runs : `gh run list --repo christiandrochon/sma-eventsourcing-17`
+  - rerun : `gh run rerun <run-id>`
+
+5) Changements que j'ai déjà appliqués (PRs récentes)
+
+J'ai appliqué plusieurs corrections et améliorations dans des branches/PRs séparées. Ci‑dessous la liste consolidée (fichier -> raison -> PR) :
+
+- `backend/src/test/resources/application-test.properties`
+  - Suppression de `MODE=PostgreSQL` dans l'URL H2 pour éviter l'erreur H2 Unknown data type "TINYINT" lors de la création du schéma en tests. (PR #4 — branch `fix/h2-test-url`)
+
+- `frontend/src/test/java/fr/cdrochon/thymeleaffrontend/configuration/TestSecurityConfig.java`
+  - Nettoyage de commentaires supprimant du code commenté trop long (corrige Sonar S125). (PR #4 — `fix/h2-test-url`)
+
+- `.gitignore`
+  - Ajout de `backend-openapi.pid` pour éviter de suivre un fichier runtime local. (PR #4 — `fix/h2-test-url`)
+
+- `frontend/src/main/java/fr/cdrochon/thymeleaffrontend/controller/vehicule/SearchVehiculeSIVController.java`
+  - Remplacement de `e.printStackTrace()` par `logger.error(...)` pour éviter les hotspots Sonar et améliorer le logging. (PR #4)
+
+- `backend/src/main/java/fr/cdrochon/smamonolithe/vehicule/command/services/VehiculeCommandService.java`
+  - Ajout de gardes null-safe dans `createVehicule` et `completeVehiculeCreation` pour corriger des NPE possibles signalés par Sonar (S2259). (PR #9 — `fix/vehicule-null-guard`)
+
+- `frontend/src/main/resources/templates/header.html`
+  - Accessibilité: remplacement des ancres utilisées comme toggles de dropdown par des `<button>` sémantiques, et ajout de handlers clavier (Enter/Espace) pour résoudre S6819. (PR #6 — `fix/header-keydown`)
+
+- `backend/src/main/java/fr/cdrochon/smamonolithe/client/command/dtos/ClientCommandDTO.java`
+  - Implémentation d'une copie défensive dans le constructeur `ClientCommandDTO(ClientAdresseDTO)` pour éviter d'exposer la référence interne de l'adresse. (PR #7 — `fix/clientdto-defensive-copy`)
+
+- `backend/src/main/java/fr/cdrochon/smamonolithe/audit/compliance/infrastructure/AuditComplianceRepository.java`
+  - Extraction des littéraux de statut (`COMPLIANT`, `PARTIAL`, `NON_COMPLIANT`, `NOT_APPLICABLE`) en constantes et refactor du SQL du dashboard pour réutiliser ces constantes. (PR #8 — `fix/audit-status-constants`)
+
+- `backend/src/main/java/fr/cdrochon/smamonolithe/client/query/controllers/ClientQueryController.java`
+  - Corrections de fiabilité recommandées par Sonar (NPE guards / logging). (inclus dans PR #10 — `consolidate/ci-reliability`)
+
+- `backend/src/main/java/fr/cdrochon/smamonolithe/client/query/services/ClientEventHandlerService.java`
+  - Corrections de fiabilité recommandées par Sonar (NPE guards / defensive copies). (inclus dans PR #10)
+
+- `.github/workflows/*` (5 workflows)
+  - Ajout d'en‑têtes commentés expliquant le rôle de chaque workflow et comment activer Sonar via Secrets (documentation). (PR #10)
+
+- `docs/CI.md`
+  - Ajout de la documentation CI/Sonar et description des fichiers de configuration (`.gitattributes`, `.editorconfig`, `sonar-project.properties`). (PR #11 — `docs/ci-update`)
+
+- `pom.xml` et fichiers de build
+  - Modifications mineures (encodage UTF‑8, exclusions Sonar, configuration d'analyse) introduites pour rendre l'analyse Sonar plus stable et éviter des warnings d'encodage. Ces changements ont été inclus dans les commits liés à la consolidation et à la préparation de Sonar (voir PR #10). (branchs : divers)
+
+Notes :
+- Chaque changement a été poussé dans une branche dédiée et ouvert en PR. Liste des PRs créées :
+  - #4 `fix/h2-test-url`
+  - #5 `docs/ci-add`
+  - #6 `fix/header-keydown`
+  - #7 `fix/clientdto-defensive-copy`
+  - #8 `fix/audit-status-constants`
+  - #9 `fix/vehicule-null-guard`
+  - #10 `consolidate/ci-reliability` (regroupe les commentaires CI + corrections reliability)
+  - #11 `docs/ci-update` (mise à jour de la doc CI)
+
+Si tu veux que j'ajoute explicitement le diff/les commits (hashs) pour chaque fichier dans la doc, je peux l'insérer également.

Contact
-------
Si tu veux que je crée la PR maintenant, réponds "Crée la PR". Si tu préfères que j'ajoute aussi des commentaires en-tête dans les fichiers YAML, réponds "Ajoute les commentaires CI".

---
Fait le: 2026-05-29
