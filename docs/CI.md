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

3) SonarCloud / SonarQube — mode opératoire
- Pour SonarCloud (cloud) : créez un token dans SonarCloud (Account → Security) puis stockez-le dans GitHub Secrets comme `SONAR_TOKEN`.
- Le workflow Sonar du repo est conditionnel: s'il trouve `SONAR_TOKEN` il exécute `mvn sonar:sonar` et pousse le résultat vers SonarCloud.
- Lancement local (exemple) sans exposer le token dans l'historique shell :

```bash
cd /home/cdn/IdeaProjects/sma-eventsourcing-17/backend
read -s SONAR_TOKEN
export SONAR_TOKEN
mvn clean verify -Daxon.axonserver.enabled=false \
  -Dsonar.host.url="https://sonarcloud.io" \
  -Dsonar.login="$SONAR_TOKEN" \
  sonar:sonar
```

4) Commandes utiles (CI / debug)
- Forcer un rerun des workflows (rapide): pousser un commit vide

```bash
git checkout -b docs/ci-add
git add docs/CI.md
git commit -m "docs(ci): add CI & SonarCloud documentation"
git push -u origin docs/ci-add
# puis créer la PR (si gh CLI est configuré)
gh pr create --title "docs(ci): add CI & Sonar documentation" --body "Ajoute docs/CI.md : description des workflows et procédure SonarCloud." --base main
```

- Alternative pour relancer CI sans PR: dans GitHub Actions → choisir le run → "Re-run jobs". Avec `gh` :
  - lister runs : `gh run list --repo christiandrochon/sma-eventsourcing-17`
  - rerun : `gh run rerun <run-id>`

5) Changements que j'ai déjà appliqués (branche `fix/h2-test-url`, PR #4)
- /home/cdn/IdeaProjects/sma-eventsourcing-17/backend/src/test/resources/application-test.properties
  - Suppression de `MODE=PostgreSQL` dans l'URL H2 (évite Unknown data type "TINYINT" pendant la création du schéma H2).

- /home/cdn/IdeaProjects/sma-eventsourcing-17/frontend/src/test/java/fr/cdrochon/thymeleaffrontend/configuration/TestSecurityConfig.java
  - Nettoyage des commentaires pour éviter l'avertissement Sonar S125 (code commenté). Le bean reste conditionnel.

- /home/cdn/IdeaProjects/sma-eventsourcing-17/.gitignore
  - Ajout de `backend-openapi.pid` pour éviter un fichier runtime en suivi.

- /home/cdn/IdeaProjects/sma-eventsourcing-17/frontend/src/main/java/fr/cdrochon/thymeleaffrontend/controller/vehicule/SearchVehiculeSIVController.java
  - Remplacement de `e.printStackTrace()` par `logger.error(...)` (évite hotspot S4507).

- /home/cdn/IdeaProjects/sma-eventsourcing-17/backend/src/main/java/fr/cdrochon/smamonolithe/vehicule/command/services/VehiculeCommandService.java
  - Ajout de gardes null-safe dans `createVehicule` et `completeVehiculeCreation` pour corriger S2259 (NPE possible).

6) Que vérifier dans SonarCloud / GitHub après PR
- Dans l'onglet «Checks» de la PR, vérifier que les workflows se terminent en «success».
- Dans SonarCloud :
  - Quality Gate (Passed/Failed) — défini par le projet SonarCloud.
  - Issues ouvertes sur la branche : filtrer par type (Bug, Vulnerability, Code Smell), règle (ex: S125,S2259,S4507).
  - Coverage on New Code : générer rapports JaCoCo si vous voulez voir coverage.

7) Próchaines actions proposées
- Si tu confirmes, je vais :
  1) créer la branche `docs/ci-add`, committer `docs/CI.md`, la pousser et ouvrir une PR (titre + description). (action que je peux faire maintenant)
  2) (optionnel) annoter les 5 fichiers de workflow en insérant un court commentaire header décrivant leur but — je peux l'ajouter dans la PR de documentation.

Contact
-------
Si tu veux que je crée la PR maintenant, réponds "Crée la PR". Si tu préfères que j'ajoute aussi des commentaires en-tête dans les fichiers YAML, réponds "Ajoute les commentaires CI".

---
Fait le: 2026-05-29

