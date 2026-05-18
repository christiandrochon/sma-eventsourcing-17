# SMA - Application de Maintenance Automobile

Application intranet de maintenance automobile basée sur Spring Boot, avec separation claire entre interface serveur (Thymeleaf) et coeur metier CQRS/Event Sourcing.

## Demarrage immediat (depuis zero)

> **Important :** pour ce projet, le lancement standard se fait via `./scripts/dev.sh`.
> N'utilisez pas `docker compose up` comme commande principale de run applicatif.

Si vous partez de zero (volumes inexistants ou supprimés), lancez simplement :

```bash
chmod +x scripts/dev.sh
./scripts/dev.sh secure
./scripts/dev.sh open
```

Mode debug rapide (sans login frontend/backend) :

```bash
./scripts/dev.sh fast
```

## Sommaire

- [1. Vue d'ensemble](#1-vue-densemble)
- [2. Architecture actuelle](#2-architecture-actuelle)
  - [2.1 Runtime local (Docker Compose)](#21-runtime-local-docker-compose)
  - [2.2 Structure du depot](#22-structure-du-depot)
  - [2.3 Flux CQRS simplifie](#23-flux-cqrs-simplifie)
- [3. Stack technique](#3-stack-technique)
- [4. Structure des modules](#4-structure-des-modules)
- [5. Demarrage rapide](#5-demarrage-rapide)
- [5.1 Demarrage officiel via script](#51-demarrage-officiel-via-script)
- [5.2 Demarrage local (sans conteneur app)](#52-demarrage-local-sans-conteneur-app)
- [6. Build et tests](#6-build-et-tests)
- [7. Configuration et profils Spring](#7-configuration-et-profils-spring)
- [8. Logs](#8-logs)
- [9. Grille d'audit independant (RGPD + gouvernance data)](#9-grille-daudit-independant-rgpd--gouvernance-data)
- [10. Troubleshooting rapide](#10-troubleshooting-rapide)
- [11. Keycloak - Realm, persistance et gestion sans login manuel](#11-keycloak---realm-persistance-et-gestion-sans-login-manuel)
  - [11.1 Roles metier (RBAC)](#111-roles-metier-rbac)
  - [11.2 Correction IDOR (lecture)](#112-correction-idor-lecture)
- [12. Pattern de creation CQRS/Axon (ce qui a ete implemente)](#12-pattern-de-creation-cqrsaxon-ce-qui-a-ete-implemente)

## 1. Vue d'ensemble

Le repository est maintenant organise en **multi-modules Maven** :

- `backend` : API metier reactive + Axon (Command/Query/Event)
- `frontend` : UI serveur Spring MVC + Thymeleaf
- `pom.xml` racine : agregateur Maven
- `compose.yaml` : stack locale complete

Objectif principal de cette architecture : isoler les responsabilites, fiabiliser les evolutions, et garder un pipeline de build/deploiement simple par module.

## 2. Architecture actuelle

### 2.1 Runtime local (Docker Compose)

```text
+-----------------------------------------------------------------------------------+
| NIVEAU 1 - CLIENT                                                                 |
|   [ Navigateur ]                                                                  |
+-----------------------------------------------------------------------------------+
                                      |
                                      | HTTP :8091
                                      v
+-----------------------------------------------------------------------------------+
| NIVEAU 2 - PRESENTATION                                                           |
|   [ Frontend - Spring MVC + Thymeleaf ]                                           |
+-----------------------------------------------------------------------------------+
                                      |
                                      | REST HTTP :8092
                                      v
+-----------------------------------------------------------------------------------+
| NIVEAU 3 - COEUR METIER                                                           |
|   [ Backend - Spring WebFlux + Axon ]                                             |
|      |                                                                            |
|      +--> Commandes + evenements --> [ Axon Server ] (:8024 / :8124)             |
|      |                                                                            |
|      +--> Projections lecture -----> [ PostgreSQL ] (:5432)                       |
+-----------------------------------------------------------------------------------+
                                      ^
                                      |
                         [ pgAdmin ] (:6002)
```

### 2.2 Structure du depot

```text
sma-eventsourcing-17/
|
+-- pom.xml                              (parent Maven, packaging pom)
|
+-- backend/                             (module metier)
|   |
|   +-- pom.xml
|   +-- Dockerfile
|   +-- application-local.properties
|   +-- application-prod.properties
|   `-- src/
|       +-- main/
|       `-- test/
|
+-- frontend/                            (module UI Thymeleaf)
|   |
|   +-- pom.xml
|   +-- Dockerfile
|   +-- application-local.properties
|   +-- application-prod.properties
|   `-- src/
|       +-- main/
|       `-- test/
|
+-- compose.yaml                         (stack locale)
+-- docker/                              (fichiers annexes)
`-- komp-smb/                            (manifests Kubernetes)
```

### 2.3 Flux CQRS simplifie

```text
+-----------------------------------------------------------------------------------+
| NIVEAU 1 - UI                                                                      |
|   [ Templates Thymeleaf ]                                                          |
+-----------------------------------------------------------------------------------+
          |
          +--> Flux ecriture (COMMAND)
          |      |
          |      v
          |   [ Controllers Command ]
          |      |
          |      v
          |   [ Axon Command Bus / Event Store ]
          |      |
          |      v
          |   [ Event Handlers / Projection ]
          |      |
          |      v
          |   [ Read Model PostgreSQL ]
          |
          `--> Flux lecture (QUERY)
                 |
                 v
              [ Controllers Query ]
                 |
                 v
              [ Read Model PostgreSQL ]
```

## 3. Stack technique

- Java 17
- Spring Boot 3.4.0
- Backend : Spring WebFlux, Axon Framework, JPA
- Frontend : Spring MVC, Thymeleaf, Thymeleaf Layout Dialect
- Base de donnees : PostgreSQL
- Orchestration locale : Docker Compose
- Build : Maven

## 4. Structure des modules

```text
sma-eventsourcing-17/
  pom.xml
  backend/
    pom.xml
    Dockerfile
    application-local.properties
    application-prod.properties
    src/main
    src/test
    logs/
  frontend/
    pom.xml
    Dockerfile
    application-local.properties
    application-prod.properties
    src/main
    src/test
    logs/
  compose.yaml
  docker/
  komp-smb/
```

## 5. Demarrage rapide

Prerequis minimaux : Docker + Docker Compose + Java 17 + Maven.

### 5.1 Demarrage officiel via script

**Regle simple : pour lancer l'application, utilisez `scripts/dev.sh` (point d'entree unique).**

**Commande qui fonctionne en partant de rien :**

```bash
chmod +x scripts/dev.sh
./scripts/dev.sh secure
```

Puis (optionnel) pour ouvrir les URLs utiles :

```bash
./scripts/dev.sh open
```

Mode rapide (sans login frontend/backend, pratique pour debug court) :

```bash
./scripts/dev.sh fast
```

Arret de la stack :

```bash
./scripts/dev.sh down
```

Commandes utiles via `dev.sh` :

```bash
./scripts/dev.sh status
./scripts/dev.sh check
./scripts/dev.sh seed-users
./scripts/dev.sh ensure-users
./scripts/dev.sh realm-status
```

Guide de choix rapide (quoi faire selon votre besoin) :

| Je veux... | Commande | Ce que ca fait | Arrete la stack ? |
|---|---|---|---|
| Lancer l'appli en mode securise (Keycloak ON) | `./scripts/dev.sh secure` | Lance la stack complete + seed users demo si possible | Non |
| Lancer l'appli vite sans login (debug court) | `./scripts/dev.sh fast` | Lance la stack avec securite frontend/backend desactivee | Non |
| Voir les URLs + credentials | `./scripts/dev.sh show` | Affiche frontend, backend, keycloak + comptes demo | Non |
| Ouvrir directement l'appli et Keycloak | `./scripts/dev.sh open` | Meme affichage + tentative d'ouverture navigateur | Non |
| Verifier que tout repond | `./scripts/dev.sh check` | Checks HTTP frontend/backend/keycloak | Non |
| Creer/mettre a jour les users demo | `./scripts/dev.sh seed-users` | Cree/maj `admin-test`, `user-test`, `audit-test` | Non |
| Garantir les users avant login (alias explicite) | `./scripts/dev.sh ensure-users` | Meme action que `seed-users`, nom plus parlant pour demarrer vite | Non |
| Verifier realm/roles/users Keycloak | `./scripts/dev.sh realm-status` | Controle du realm et roles via API admin | Non |
| Arreter la stack | `./scripts/dev.sh down` | `docker compose down` | Oui |
| Redemarrer proprement en mode secure | `./scripts/dev.sh restart-secure` | `down` puis `secure` | Oui (puis relance) |
| Redemarrer proprement en mode fast | `./scripts/dev.sh restart-fast` | `down` puis `fast` | Oui (puis relance) |

### 5.2 Demarrage local (sans conteneur app)

Option pratique : lancer seulement les dependances (PostgreSQL + Axon + pgAdmin) en Docker, puis demarrer les apps en local.

> Cette section est un mode avance. Pour un lancement standard, revenez a `./scripts/dev.sh secure`.

1) Lancer les dependances :

```bash
docker compose -f compose.yaml up -d postgres-monolithe axon-server pgadmin4
```

2) Lancer le backend en local avec surcharge `application-local.properties` :

```bash
cd /home/cdn/IdeaProjects/sma-eventsourcing-17/backend
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local,--spring.config.additional-location=file:application-local.properties"
```

3) Lancer le frontend en local avec surcharge `application-local.properties` :

```bash
cd /home/cdn/IdeaProjects/sma-eventsourcing-17/frontend
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local,--spring.config.additional-location=file:application-local.properties"
```

Acces utiles :

- Frontend : `http://localhost:8091`
- Backend : `http://localhost:8092`
- Health backend : `http://localhost:8092/actuator/health`
- Axon Dashboard : `http://localhost:8024`
- pgAdmin : `http://localhost:6002`

## 6. Build et tests

Cette section sert a valider que la base compile, que les regressions principales sont detectees, et que les refactorings n'ont pas casse le flux metier.

Build complet (racine) :

```bash
mvn clean verify
```

Build par module :

```bash
mvn -pl backend -DskipTests compile
mvn -pl frontend -DskipTests compile
```

Tests backend uniquement :

```bash
mvn -pl backend test
```

Tests frontend uniquement :

```bash
mvn -pl frontend test
```

Tests cibles backend (log technique) :

```bash
mvn -pl backend -Dtest=TechnicalRequestWebFilterTest,GlobalTechnicalExceptionHandlerTest,ClientWebConfigTest test
```

## 7. Configuration et profils Spring

Profils utilises :

- `default` : valeurs de base dans `src/main/resources/application.properties`
- `local` : surcharge locale via `application-local.properties` (fichiers a la racine des modules)
- `prod` : surcharge conteneur via `application-prod.properties`

Fichiers principaux :

- `backend/application-local.properties`
- `backend/application-prod.properties`
- `frontend/application-local.properties`
- `frontend/application-prod.properties`

Important : `application-local.properties` et `application-prod.properties` sont des fichiers externes aux resources Spring standards. Pour les prendre en compte de facon explicite, utiliser `--spring.config.additional-location=file:...` comme dans la section demarrage.

## 8. Logs

Cette section sert a distinguer rapidement les evenements metier (audit fonctionnel) des erreurs techniques (diagnostic runtime).

Point important : le log technique est gere des deux cotes, mais pas pour les memes usages.

- **Frontend** : diagnostic UI, requetes HTTP entrantes, appels WebClient vers le backend, initialisation du module.
- **Backend** : diagnostic serveur, requetes WebFlux entrantes, exceptions techniques globales.

Les logs de sécurité sont maintenant separes dans un dossier dédié `securite` :

- **Frontend** : tentatives d'accès non autorisées, accès refusés, méthodes ou chemins suspects.
- **Backend** : requêtes suspectes, accès refusés, chemins/méthodes anormaux.

Separation actuelle des logs :

- Backend metier : `backend/logs/metier/business.log`
- Backend technique : `backend/logs/technique/technical.log`
- Backend sécurité : `backend/logs/security.log`
- Frontend acces UI : `frontend/logs/metier/ui-access.log`
- Frontend metier UI : `frontend/logs/metier/ui-business.log`
- Frontend erreurs UI : `frontend/logs/technique/ui-error.log`
- Frontend technique : `frontend/logs/technique/ui-technical.log`
- Frontend sécurité : `frontend/logs/securite/ui-security.log`

En pratique :

- un evenement `UI_ACCESS_*` va dans `frontend/logs/metier/ui-access.log`
- un evenement `UI_TECH_*` va dans `frontend/logs/technique/ui-technical.log`
- un evenement `SEC_*` frontend va dans `frontend/logs/securite/ui-security.log`
- un evenement backend technique va dans `backend/logs/technique/technical.log`
- un evenement `SEC_*` backend va dans `backend/logs/security.log`

Suivi live des logs metier backend :

```bash
tail -f backend/logs/metier/business.log
```

Suivi live des logs techniques backend :

```bash
tail -f backend/logs/technique/technical.log
```

Suivi live des logs sécurité backend :

```bash
tail -f backend/logs/security.log
```

Suivi live des logs frontend :

```bash
tail -f frontend/logs/metier/ui-access.log
tail -f frontend/logs/metier/ui-business.log
tail -f frontend/logs/technique/ui-error.log
tail -f frontend/logs/technique/ui-technical.log
tail -f frontend/logs/securite/ui-security.log
```

Validation automatique des logs de securite :

- Backend :
  - `TechnicalRequestWebFilterHttpStatusMatrixTest` (100 cas, statuts 2xx a 5xx)
  - `TechnicalRequestWebFilterSecurityAnomalyTest` (methodes/chemins suspects + cas normal)
  - `BackendSecurityLogFileDetectionTest` (30+ cas : codes 3xx/4xx/5xx, verification d'ecriture reelle dans `security.log`)
  - `BackendLoggingStartupProbeTest` (verification de la creation des fichiers de log au demarrage)
- Frontend :
  - `FrontendTechnicalRequestFilterHttpStatusMatrixTest` (100 cas, statuts 2xx a 5xx)
  - `FrontendTechnicalRequestFilterSecurityAnomalyTest` (methodes/chemins suspects + cas normal)
  - `FrontendSecurityLogFilePolicyTest` (30+ cas : codes 3xx/4xx/5xx, verification d'ecriture reelle dans `ui-security.log`)

### Politique de log securite

Les regles appliquees automatiquement par les filtres de securite sont :

| Situation | Code HTTP | Evenement logue | Fichier cible |
|---|---|---|---|
| Acces non autorise | 401 | `SEC_HTTP_ACCESS_DENIED` / `SEC_FRONTEND_ACCESS_DENIED` | `security.log` / `ui-security.log` |
| Acces interdit | 403 | `SEC_HTTP_ACCESS_DENIED` / `SEC_FRONTEND_ACCESS_DENIED` | `security.log` / `ui-security.log` |
| Methode suspecte (DELETE, PATCH, PUT, TRACE, OPTIONS) | tout | `SEC_HTTP_ANOMALOUS_REQUEST` / `SEC_FRONTEND_ANOMALOUS_REQUEST` | `security.log` / `ui-security.log` |
| Chemin suspect (`//`, `../`, `/admin`, `/actuator`, etc.) | tout | `SEC_HTTP_ANOMALOUS_REQUEST` / `SEC_FRONTEND_ANOMALOUS_REQUEST` | `security.log` / `ui-security.log` |
| Autres codes (2xx, 3xx, 4xx autres, 5xx) | hors 401/403 | **rien** (pas de log securite) | — |

Seuls les codes **401** et **403** alimentent le log securite pour les acces refuses. Les codes 3xx, 400, 404, 429, 500, 502, 503, etc. ne declenchent pas d'evenement securite (ils sont geres par le log technique).

Execution rapide des tests de securite :

```bash
mvn -pl backend -Dtest=TechnicalRequestWebFilterHttpStatusMatrixTest,TechnicalRequestWebFilterSecurityAnomalyTest,BackendSecurityLogFileDetectionTest,BackendLoggingStartupProbeTest test
mvn -pl frontend -Dtest=FrontendTechnicalRequestFilterHttpStatusMatrixTest,FrontendTechnicalRequestFilterSecurityAnomalyTest,FrontendSecurityLogFilePolicyTest test
```

### Creation automatique des fichiers de log au demarrage

Au demarrage de chaque module, un composant `@Component` (`BackendLoggingStartupProbe` / `FrontendLoggingStartupProbe`) verifie et cree automatiquement les fichiers de log manquants :

- Backend : `backend/logs/metier/`, `backend/logs/technique/`, `backend/logs/security.log`
- Frontend : `frontend/logs/metier/`, `frontend/logs/technique/`, `frontend/logs/securite/`

Si un fichier est absent au demarrage, il est cree vide. Un evenement `SEC_STARTUP` est emis dans le log securite pour confirmer l'initialisation.

```bash
# Verifier la creation des fichiers au premier lancement :
ls -la backend/logs/
ls -la frontend/logs/securite/
```

## 9. Grille d'audit independant (RGPD + gouvernance data)

Objectif : rendre consultables en base les attentes d'un audit independant, au-dela des logs techniques.

Cette grille fonctionne en 3 blocs complementaires :

- `audit_events` : preuves operationnelles (qui a consulte quoi, quand, resultat, ip, etc.)
- `audit_expectations` : referentiel des attentes de conformite (ce que l'auditeur verifie)
- `audit_expectation_checks` : historique des controles independants (statut, score, constats, preuves)

### Ce que fait la grille

- Centralise les attentes RGPD et gouvernance de donnees dans la base `audit`
- Permet d'associer des preuves concretes et datées a chaque attente
- Conserve l'historique des controles (pas seulement le dernier statut)
- Fournit une vue `audit_expectations_latest` pour un cockpit rapide
- Garde les tables d'audit en mode append-only (pas d'update/delete)

### Grille d'attentes initiale (seed)

| Code | Domaine | Attente auditee | Frequence cible |
|---|---|---|---|
| `GOV_001` | `GOVERNANCE` | Roles et responsabilites definis | `QUARTERLY` |
| `LAW_001` | `LEGAL_BASIS` | Base legale documentee | `QUARTERLY` |
| `ROPA_001` | `ROPA` | Registre des traitements maintenu | `MONTHLY` |
| `MIN_001` | `MINIMIZATION` | Minimisation des donnees | `QUARTERLY` |
| `RET_001` | `RETENTION` | Retention et suppression controlees | `MONTHLY` |
| `DSR_001` | `DATA_SUBJECT_RIGHTS` | Gestion des droits des personnes | `MONTHLY` |
| `IAM_001` | `ACCESS_CONTROL` | Moindre privilege / habilitations | `MONTHLY` |
| `AUD_001` | `TRACEABILITY` | Traçabilite complete et consultable | `WEEKLY` |
| `XGR_001` | `CROSS_GARAGE` | Acces cross-garage monitorés | `WEEKLY` |
| `VND_001` | `PROCESSORS` | Sous-traitants et contrats maitrises | `QUARTERLY` |
| `TRF_001` | `INTERNATIONAL_TRANSFER` | Transferts internationaux couverts | `QUARTERLY` |
| `DPIA_001` | `DPIA` | Analyses d'impact sur traitements a risque | `QUARTERLY` |
| `INC_001` | `INCIDENTS` | Processus de violation de donnees operationnel | `MONTHLY` |
| `RES_001` | `RESILIENCE` | Sauvegarde/restauration testees | `MONTHLY` |
| `QTY_001` | `DATA_QUALITY` | Qualite/integrite des donnees | `MONTHLY` |

### API backend pour consultation d'audit

Ces endpoints lisent/ecrivent dans la base `audit` (datasource dediee `audit.datasource.*`) :

- `GET /audit/compliance/expectations` : liste la grille + dernier statut de chaque attente
- `GET /audit/compliance/expectations/{code}` : detail d'une attente + historique des controles
- `POST /audit/compliance/expectations/{code}/checks` : ajoute une evaluation independante
- `GET /audit/compliance/dashboard` : synthese (statuts + metriques 30 jours)

### Procedure complete (mise en place + execution d'un audit)

Checklist operationnelle :

1. Demarrer l'infra (`postgres-monolithe`, `backend`)
2. Initialiser la base `audit` et le schema (si volume deja existant)
3. Verifier que la grille seedee est presente
4. Executer la collecte de preuves (`audit_events`)
5. Saisir les controles independants (`audit_expectation_checks`)
6. Exporter la synthese (`dashboard` + SQL)

Commandes minimales :

```bash
cd /home/cdn/IdeaProjects/sma-eventsourcing-17
docker compose -f compose.yaml up -d postgres-monolithe backend
```

```bash
# Optionnel : verifier que le backend est vivant
curl -fsS "http://localhost:8092/actuator/health"
```

```bash
# Si volume PostgreSQL deja existant, appliquer explicitement le schema audit
docker exec -i postgres-monolithe bash -lc "psql -U postgres -d postgres -tc \"SELECT 1 FROM pg_database WHERE datname='audit'\" | grep -q 1 || psql -U postgres -d postgres -c \"CREATE DATABASE audit\""
docker exec -i postgres-monolithe psql -U postgres -d audit < docker/audit_schema.sql
```

```bash
# Verifier que la grille seedee existe
docker exec -i postgres-monolithe psql -U postgres -d audit -c "SELECT code, domain, expected_frequency FROM audit_expectations ORDER BY code;"
```

```bash
# Verifier l'alimentation automatique des preuves operationnelles
curl -sS "http://localhost:8092/queries/vehicules" >/dev/null
docker exec -i postgres-monolithe psql -U postgres -d audit -c "SELECT event_time, actor, action, resource, http_status FROM audit_events ORDER BY id DESC LIMIT 10;"
```

```bash
# Lire la grille + dernier etat
curl -sS "http://localhost:8092/audit/compliance/expectations"

# Lire le dashboard de pilotage
curl -sS "http://localhost:8092/audit/compliance/dashboard"
```

```bash
# Enregistrer un controle independant (exemple)
curl -X POST "http://localhost:8092/audit/compliance/expectations/AUD_001/checks" \
  -H "Content-Type: application/json" \
  -d '{
    "checkedBy": "cabinet-externe",
    "status": "PARTIAL",
    "score": 72,
    "scope": "Perimetre backend prod",
    "findings": "Traçabilite correcte mais raison fonctionnelle parfois absente.",
    "remediationPlan": "Rendre reason obligatoire pour les acces cross-garage.",
    "dueDate": "2026-06-30",
    "evidenceUri": "s3://audit/evidence/AUD_001_2026Q2.pdf",
    "crossGarageSampleSize": 25,
    "insertedFrom": "INDEPENDENT_AUDIT"
  }'
```

```bash
# Verifier l'historique d'une attente
curl -sS "http://localhost:8092/audit/compliance/expectations/AUD_001?historyLimit=20"
```

Points de vigilance pour l'audit independant :

- `audit_events` et `audit_expectation_checks` sont append-only (triggers SQL bloquent `UPDATE/DELETE`)
- la preuve "qui a consulte quoi" est dans `audit_events`
- la preuve "pourquoi / resultat / plan de remediation" est dans `audit_expectation_checks`
- tant que l'auth JWT n'est pas reactivee, l'acteur peut apparaitre `ANONYMOUS` dans certaines traces

### Campagne audit externe complete (40 cas)

Une campagne complete est scriptable et rejouable via `scripts/audit-external-40.sh`.

Cette campagne execute 40 cas couvrant :

- infrastructure et sante runtime
- existence schema audit (`audit_events`, `audit_expectations`, `audit_expectation_checks`, vue latest)
- contraintes d'integrite (FK)
- immutabilite append-only (blocage `UPDATE/DELETE`)
- matrice RBAC API audit (`sans token`, `USER`, `AUDITOR`, `ADMIN`)
- creation d'un verdict independant et verification de persistance
- verification `scripts/audit-check.sh` et bundle export `scripts/audit-export.sh`

Execution :

```bash
cd /home/cdn/IdeaProjects/sma-eventsourcing-17
chmod +x scripts/audit-external-40.sh
./scripts/audit-external-40.sh
```

Sorties generees :

- `audit-exports/<timestamp>/audit_external_40_results.csv` (resultats case par case)
- `audit-exports/<timestamp>/audit_external_40_proof.md` (preuves lisibles)
- `audit-exports/<timestamp>/<timestamp>/...csv` (bundle export auditeur)

Derniere execution verifiee (2026-05-18) :

- preuve CSV: `audit-exports/20260518_213006/audit_external_40_results.csv`
- preuve MD: `audit-exports/20260518_213006/audit_external_40_proof.md`
- synthese: `PASS=40`, `FAIL=0`, `WARN=0`, `TOTAL=40`

Resultat :

- la campagne couvre 40 cas et passe integralement
- les checks independants sont persistes dans `audit_expectation_checks`
- les exports auditeur (CSV + manifest) sont generes dans le dossier de run

### Initialiser la grille sur une base existante (volume deja cree)

Si ton conteneur PostgreSQL utilise deja un volume, le script `initdb_postgres.sh` ne se rejoue pas automatiquement.
Dans ce cas, applique explicitement le schema via `docker/audit_schema.sql` :

```bash
docker exec -i postgres-monolithe bash -lc "psql -U postgres -d postgres -tc \"SELECT 1 FROM pg_database WHERE datname='audit'\" | grep -q 1 || psql -U postgres -d postgres -c \"CREATE DATABASE audit\""
docker exec -i postgres-monolithe psql -U postgres -d audit < docker/audit_schema.sql
```

Exemple d'ajout d'un controle d'audit :

```bash
curl -X POST "http://localhost:8092/audit/compliance/expectations/AUD_001/checks" \
  -H "Content-Type: application/json" \
  -d '{
    "checkedBy": "cabinet-externe",
    "status": "PARTIAL",
    "score": 72,
    "scope": "Perimetre backend prod",
    "findings": "Traçabilite correcte mais raison fonctionnelle parfois absente.",
    "remediationPlan": "Rendre reason obligatoire pour les acces cross-garage.",
    "dueDate": "2026-06-30",
    "evidenceUri": "s3://audit/evidence/AUD_001_2026Q2.pdf",
    "crossGarageSampleSize": 25,
    "insertedFrom": "INDEPENDENT_AUDIT"
  }'
```

### Requetes SQL utiles pour un audit independant

```sql
-- Vue synthese de la grille
SELECT code, domain, title, status, score, checked_at
FROM audit_expectations_latest
ORDER BY domain, code;

-- Historique des controles d'une attente
SELECT *
FROM audit_expectation_checks
WHERE expectation_code = 'AUD_001'
ORDER BY checked_at DESC;

-- Qui a consulte quoi (30 jours)
SELECT actor, action, resource, resource_id, event_time
FROM audit_events
WHERE event_time >= now() - interval '30 days'
ORDER BY event_time DESC;

-- Acces cross-garage
SELECT *
FROM audit_events
WHERE cross_garage = true
ORDER BY event_time DESC;
```

### Notes d'implementation

- Le schema est defini dans `docker/initdb_postgres.sh` et `docker/audit_schema.sql`
- Les tables d'audit sont append-only via trigger `prevent_audit_mutation()`
- La capture automatique des requetes HTTP passe par `TechnicalRequestWebFilter`
- Le module de consultation est expose par `AuditComplianceController`

## 10. Troubleshooting rapide

1. **Erreur Thymeleaf `Error resolving template [template]`**
   - verifier que `frontend/src/main/resources/templates/template.html` existe
   - verifier que `layout:decorate="~{template}"` est utilise dans les vues

2. **Erreur Thymeleaf `Error resolving template [toggledark]`**
   - verifier que `frontend/src/main/resources/templates/toggledark.html` existe
   - verifier l'inclusion `th:insert="~{toggledark :: toggledarkFragment}"`

3. **Le frontend ne joint pas le backend**
   - en local : `external.service.url=http://localhost:8092`
   - en docker : `external.service.url=http://backend:8092`

4. **Propagation JWT frontend incoherente (401/403 intermittents sur certaines pages)**
   - la resolution du token est centralisee dans `frontend/src/main/java/fr/cdrochon/thymeleaffrontend/security/FrontendTokenResolver.java`
   - les controllers frontend doivent appeler `tokenResolver.resolveAccessToken(authentication)` puis poser `setBearerAuth(...)`
   - ne pas reintroduire d'anciens helpers locaux (`getJwtTokenValue`, `resolveAccessToken` inline)

```bash
# Verification rapide: aucune ancienne methode locale de token
grep -R --line-number "getJwtTokenValue\|resolveAccessToken(Authentication" frontend/src/main/java

# Verification rapide: pas de bearer mal forme
grep -R --line-number "setBearerAuth(\"Bearer " frontend/src/main/java || true

# Validation compilation frontend
mvn -pl frontend -DskipTests compile
```

5. **Aucun log business visible**
   - verifier `logging.file.path` dans les fichiers `application-*.properties`
   - verifier la config de `backend/src/main/resources/logback-spring.xml`

### 10.3 Smoke-check RBAC (USER / ADMIN / AUDITOR)

Checklist fonctionnelle minimale apres changement securite/UI :

- [ ] Se connecter avec `user-test` et verifier que le menu masque `Creer un dossier` et `Rechercher un client`
- [ ] Se connecter avec `admin-test` et verifier que les deux entrees menu sont visibles
- [ ] Se connecter avec `audit-test` et verifier les pages de lecture autorisees
- [ ] Verifier qu'aucune page n'affiche `Erreur de connexion au serveur` sur les routes `clients`, `dossiers`, `vehicules`
- [ ] Verifier les logs frontend/backend en cas d'echec (statut HTTP + cause)

Commandes utiles pendant le smoke-check :

```bash
# Frontend
tail -f frontend/logs/metier/ui-access.log
tail -f frontend/logs/technique/ui-error.log
tail -f frontend/logs/securite/ui-security.log

# Backend
tail -f backend/logs/metier/business.log
tail -f backend/logs/technique/technical.log
tail -f backend/logs/security.log
```

### 10.1 FAQ audit (RGPD)

1. **`relation "audit_expectations" does not exist`**
   - la base `audit` n'a pas encore le schema
   - applique `docker/audit_schema.sql` dans le conteneur PostgreSQL

```bash
cd /home/cdn/IdeaProjects/sma-eventsourcing-17
docker exec -i postgres-monolithe bash -lc "psql -U postgres -d postgres -tc \"SELECT 1 FROM pg_database WHERE datname='audit'\" | grep -q 1 || psql -U postgres -d postgres -c \"CREATE DATABASE audit\""
docker exec -i postgres-monolithe psql -U postgres -d audit < docker/audit_schema.sql
```

2. **Le endpoint `/audit/compliance/*` repond 404**
   - verifier que le backend tourne bien sur `8092`
   - verifier que l'artefact en cours est le backend le plus recent

```bash
curl -fsS "http://localhost:8092/actuator/health"
curl -i "http://localhost:8092/audit/compliance/dashboard"
```

3. **La grille d'attentes est vide**
   - verifier que le seed a ete applique
   - compter les lignes dans `audit_expectations` (attendu: 15 lignes minimum)

```bash
docker exec -i postgres-monolithe psql -U postgres -d audit -c "SELECT COUNT(*) AS expectations_count FROM audit_expectations;"
docker exec -i postgres-monolithe psql -U postgres -d audit -c "SELECT code, domain FROM audit_expectations ORDER BY code;"
```

4. **`audit_events` ne se remplit pas**
   - declencher une requete backend, puis verifier les derniers evenements
   - en local sans JWT, `actor` peut etre `ANONYMOUS` (comportement attendu)

```bash
curl -sS "http://localhost:8092/queries/vehicules" >/dev/null
docker exec -i postgres-monolithe psql -U postgres -d audit -c "SELECT id, event_time, actor, action, resource, http_status FROM audit_events ORDER BY id DESC LIMIT 20;"
```

5. **Impossible de modifier/supprimer des lignes d'audit**
   - c'est normal: les tables d'audit sont append-only (preuve non falsifiable)
   - verifier la presence des triggers de blocage

```bash
docker exec -i postgres-monolithe psql -U postgres -d audit -c "SELECT tgname, tgrelid::regclass FROM pg_trigger WHERE tgname LIKE 'trg_audit_%';"
```

6. **Comment preparer un export auditeur rapidement ?**
   - exporter la vue `audit_expectations_latest`
   - exporter les evenements `audit_events` des 30 derniers jours

```bash
docker exec -i postgres-monolithe psql -U postgres -d audit -c "\copy (SELECT * FROM audit_expectations_latest ORDER BY domain, code) TO STDOUT WITH CSV HEADER" > audit_expectations_latest.csv
docker exec -i postgres-monolithe psql -U postgres -d audit -c "\copy (SELECT * FROM audit_events WHERE event_time >= now() - interval '30 days' ORDER BY event_time DESC) TO STDOUT WITH CSV HEADER" > audit_events_30d.csv
```

7. **Comment verifier les acces cross-garage ?**
   - cette extraction doit etre revue periodiquement (attente `XGR_001`)

```bash
docker exec -i postgres-monolithe psql -U postgres -d audit -c "SELECT event_time, actor, actor_garage, garage_id, resource, resource_id, reason FROM audit_events WHERE cross_garage = true ORDER BY event_time DESC LIMIT 200;"
```

8. **Existe-t-il une commande unique pour verifier rapidement l'etat audit ?**
   - oui, utiliser le script `scripts/audit-check.sh`
   - mode standard: diagnostic seulement
   - mode `--apply-schema`: tente de corriger le schema audit si manquant

```bash
cd /home/cdn/IdeaProjects/sma-eventsourcing-17
chmod +x scripts/audit-check.sh
./scripts/audit-check.sh
./scripts/audit-check.sh --apply-schema
```

9. **Comment exporter les preuves pour un auditeur externe ?**
   - utiliser `scripts/audit-export.sh` pour produire un dossier CSV date
   - le dossier contient un `README.txt` manifest

```bash
cd /home/cdn/IdeaProjects/sma-eventsourcing-17
chmod +x scripts/audit-export.sh
./scripts/audit-export.sh
./scripts/audit-export.sh --days 90 --output-dir ./audit-exports
```

10. **Comment eviter un login manuel repetitif avec Keycloak ?**
   - utiliser un token non interactif pour scripts/outillage
   - script fourni : `scripts/keycloak-token.sh`

```bash
cd /home/cdn/IdeaProjects/sma-eventsourcing-17
chmod +x scripts/keycloak-token.sh
export KEYCLOAK_BASE_URL="http://localhost:8080"
export KEYCLOAK_REALM="sma-realm"
export KEYCLOAK_CLIENT_ID="sma-thymeleaf-frontend"
export KEYCLOAK_GRANT_TYPE="client_credentials"
TOKEN="$(./scripts/keycloak-token.sh)"
curl -H "Authorization: Bearer ${TOKEN}" "http://localhost:8092/audit/compliance/dashboard"
```

## 11. Sécurité RBAC, propriété des ressources et corrections IDOR

### 11.0 Vue d'ensemble des règles métier

Le projet applique un modèle de sécurité à deux niveaux :

1. **Authentification JWT** : validation des tokens Keycloak, extraction des rôles via `realm_access.roles`
2. **RBAC + Ownership** : vérification que l'utilisateur peut accéder/modifier une ressource (rôle + propriété de la ressource)

Principes fondamentaux :

- **ADMIN** : accès global à toutes les ressources (clients, véhicules, documents, dossiers)
- **USER** : accès restreint aux ressources qu'il possède ou pour lesquelles il est autorisé (véhicules de son client, documents créés par son client, etc.)
- **AUDITOR** : accès lecture globale pour audit, pas de modification de données métier

### 11.1 Matrice d'accès : qui accède à quoi

#### Clients
- **ADMIN LIST** : tous les clients  
- **USER LIST** : uniquement si propriétaire du client (selon `mailClient`)
- **ADMIN BY-ID** : n'importe quel client  
- **USER BY-ID** : uniquement si propriétaire (`403 FORBIDDEN` sinon)
- **CREATE** : ADMIN uniquement (l'ownership client est attribué à la création)

#### Véhicules
- **ADMIN LIST** : tous les véhicules  
- **USER LIST** : uniquement les véhicules du client propriétaire (`vehicule.client.mailClient == JWT email`)
- **ADMIN BY-ID** : n'importe quel véhicule  
- **USER BY-ID** : uniquement si `vehicule.client.mailClient == JWT email` (`403 FORBIDDEN` sinon)
- **CREATE** : ADMIN ou USER (le véhicule est rattaché au client JWT)

#### Documents
- **ADMIN LIST** : tous les documents  
- **USER LIST** : uniquement les documents du client propriétaire (`document.client.mailClient == JWT email`)
- **ADMIN BY-ID** : n'importe quel document  
- **USER BY-ID** : uniquement si `document.client.mailClient == JWT email` (`403 FORBIDDEN` sinon)
- **CREATE** : ADMIN ou USER (le document est rattaché au client JWT; voir propagation `clientId` ci-dessous)

#### Dossiers
- **ADMIN LIST** : tous les dossiers  
- **USER LIST** : uniquement les dossiers du client propriétaire (`dossier.client.mailClient == JWT email`)
- **ADMIN BY-ID** : n'importe quel dossier  
- **USER BY-ID** : uniquement si `dossier.client.mailClient == JWT email` (`403 FORBIDDEN` sinon)
- **CREATE** : ADMIN ou USER (le dossier est rattaché au client JWT)

| Ressource | ADMIN LIST | USER LIST (filtré) | ADMIN BY-ID | USER BY-ID (ownership) | CREATION | Propriété |
|---|---|---|---|---|---|---|
| Client | ✅ tous | ⚠️ filtre email | ✅ n'importe quel | ⚠️ email JWT | ❌ ADMIN seul | `mailClient` |
| Véhicule | ✅ tous | ✅ `client.mailClient` | ✅ n'importe quel | ✅ `client.mailClient` | ✅ lié au JWT | `client.id` |
| Document | ✅ tous | ✅ `client.mailClient` | ✅ n'importe quel | ✅ `client.mailClient` | ✅ lié au JWT | `client.id` |
| Dossier | ✅ tous | ✅ `client.mailClient` | ✅ n'importe quel | ✅ `client.mailClient` | ✅ lié au JWT | `client.id` |

### 11.2 Implémentation IDOR (Insecure Direct Object Reference)

#### Endpoints protégés et comportement

Tous les endpoints query appliquent la logique suivante :

```java
// Extraire l'email JWT et déterminer si admin
String email = authentication.getName();  // email du JWT
boolean isAdmin = authentication.getAuthorities().stream()
    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

// Si lecture par-id
if (id != null) {
    Resource resource = findById(id);
    if (!isAdmin) {
        String ownerEmail = resource.getClient().getMailClient();  // ou resource.owner
        if (!email.equals(ownerEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Acces refuse: ressource appartient a " + ownerEmail);
        }
    }
    return resource;
}

// Si lecture liste
if (!isAdmin) {
    return findByClientMailClient(email);  // retourner uniquement les ressources du propriétaire
} else {
    return findAll();  // admin = tout voir
}
```

#### Validation JWT et extraction d'email

Dans le backend, toute méthode de query controller reçoit un paramètre `Authentication` :

```java
@GetMapping("/queries/vehicules")
public ResponseEntity<List<VehiculeDTO>> getAll(@ParameterObject Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    String email = authentication.getName();
    boolean isAdmin = hasRole(authentication, "ADMIN");
    // ...
}
```

Les réjectes de USER sans JWT email :

```java
if (!isAdmin) {
    if (email == null || email.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
            "Acces refuse: utilisateur sans email JWT");
    }
    // ...
}
```

### 11.3 Propagation `clientId` en création (CQRS flow)

#### Flux complet document (applicable à véhicule/dossier)

Quand un USER crée un document, le `clientId` doit être déduit et propagé jusqu'à la persistance :

```text
[1] Frontend POST /commands/documents
    {name: "...", content: "..."}

[2] DocumentCommandController
    -> Authentication -> JWT email
    -> ClientRepository.findByMailClient(email)
    -> documentDTO.setClientId(client.id)

[3] DocumentCreateCommand
    constructor(name, content, clientId)

[4] DocumentAggregate @CommandHandler
    -> apply(new DocumentCreatedEvent(id, name, content, clientId))

[5] DocumentEventHandlerService @EventHandler(DocumentCreatedEvent)
    -> documentEntity.setClient(clientRepository.findById(clientId))
    -> documentRepository.save(documentEntity)
    -> publish(new DocumentCreatedApplicationEvent(documentDTO))

[6] DocumentQueryController (later read)
    -> document.getClient().getMailClient() == JWT email? OK : 403

```

Classes clés :

- `backend/src/main/java/.../document/command/dtos/DocumentCommandDTO.java` : ajout `clientId`
- `backend/src/main/java/.../document/command/commands/DocumentCreateCommand.java` : ajout constructeur avec `clientId`
- `backend/src/main/java/.../document/events/DocumentCreatedEvent.java` : ajout paramètre `clientId`
- `backend/src/main/java/.../document/query/entities/Document.java` : `@ManyToOne Client client`
- `backend/src/main/java/.../document/query/services/DocumentEventHandlerService.java` : setter `client` après query

#### Gestion du cas USER sans client

Si un USER n'a pas de client associé (cas d'erreur) :

```java
Optional<Client> clientOpt = clientRepository.findByMailClient(email);
if (clientOpt.isEmpty()) {
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
        "Aucun client associe au mail JWT : " + email);
}
documentDTO.setClientId(clientOpt.get().getId());
// ... poursuivre
```

### 11.4 Schéma et migrations Flyway

#### Modifications apportées

Le schéma d'ownership a été introduit progressivement via Flyway :

- **V1__create_schema.sql** : crée tous les rôles avec FK client_id dès la création
  ```sql
  CREATE TABLE document (
      id VARCHAR(255) PRIMARY KEY,
      client_id VARCHAR(255),
      ...
      CONSTRAINT fk_document_client FOREIGN KEY (client_id) REFERENCES client(id)
  );
  ```

- **V2__insert_sample_data.sql** : seed data cohérent
  - 4 clients (`cli-0001-demo` à `cli-0004-demo`)
  - 6 véhicules (2 à 3 par client)
  - 6 dossiers (1-2 par client)
  - 14 documents (2-4 par client)
  - Chaque entité inclut `client_id`

- **V3__add_client_to_document.sql** : migration de rattrapage (idempotente) pour les bases existantes
  ```sql
  DO $$
  BEGIN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns 
      WHERE table_name = 'document' AND column_name = 'client_id'
    ) THEN
      ALTER TABLE document ADD COLUMN client_id VARCHAR(255);
      ALTER TABLE document ADD CONSTRAINT fk_document_client 
        FOREIGN KEY (client_id) REFERENCES client(id);
    END IF;
  END $$;
  ```

#### Basculer sur Flyway

Après implémentation, la config de chaque profil (`local`, `prod`, `test`) inclut :

```properties
# application.properties (default)
spring.jpa.hibernate.ddl-auto=none
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0

# application-test.properties (pour tests unitaires)
spring.flyway.enabled=false
```

### 11.5 Tests RBAC et IDOR (50 cas)

Fichier : `backend/src/test/java/.../security/RbacUserAdminMatrix50Test.java`

Couverture : 50 tests dynamiques couvrant les scénarios RBAC/IDOR :

- **Création (8 cas)** : USER/ADMIN crée document/vehicule/dossier, vérification ownership
- **Lecture liste (12 cas)** : USER/ADMIN reçoit données filtrées/complètes
- **Lecture par-id (12 cas)** : USER/ADMIN accède/refuse sur propriété
- **Cas limites (18 cas)** : USER sans JWT, sans client, ADMIN bypassé, etc.

Exemples concrets :

```java
@Test
@DisplayName("USER_CREATE_DOC_THEN_READ_OWN")
public void testUserCreatesDocumentThenReadsOwn() {
    // Crée un document avec JWT email user@client-0001.demo
    // Relit le document, vérifie que c'est le bon client.id
    // Assertion : status OK, document.client.id == cli-0001-demo
}

@Test
@DisplayName("USER_CREATE_DOC_THEN_CANNOT_READ_OTHER_CLIENT")
public void testUserCreatesDocumentButCannotReadOtherClient() {
    // User A crée un document avec client A
    // User B tente de lire ce document avec JWT email user@client-0002.demo
    // Assertion : status 403 FORBIDDEN, message "ressource appartient à ..."
}

@Test
@DisplayName("ADMIN_READ_ALL_DOCS_NO_FILTER")
public void testAdminCanReadAllDocumentsNoFilter() {
    // Admin lit /queries/documents sans filtre
    // Assertion : status OK, 14+ documents de tous les clients
}
```

Exécution :

```bash
mvn -pl backend -Dtest=RbacUserAdminMatrix50Test test
```

Attendu : **50/50 passed** ✅

---

## 11. Keycloak — Realm, persistance et gestion sans login manuel

### Pourquoi le realm ne s'effondre pas au redemarrage ?

Keycloak stocke toutes ses donnees (realm, clients, roles, utilisateurs) dans **PostgreSQL**, et ce PostgreSQL possede un **volume Docker nomme** `pg_keycloak` dans `compose.yaml`. Ce volume persiste sur le disque hote entre les arrets/redemarrages de Docker.

```
Docker stop/start
      │
      ▼
postgres-keycloak  ──────►  volume pg_keycloak  (disque hote)
      │                              │
      ▼                              ▼
Keycloak repart    ◄──── données realm intactes
```

**La seule situation ou le realm disparait** : si vous supprimez explicitement le volume avec `docker compose down -v`.

---

### Fichier realm-export.json — filet de secours

Le fichier `docker/realm-export.json` est la **sauvegarde complete du realm** :
- Roles : `ADMIN`, `USER`, `AUDITOR`
- Clients : `sma-thymeleaf-frontend`, `sma-monolithe`, etc.
- Configuration SSO, durees des tokens, politiques mot de passe

Au demarrage initial (volume vide), Keycloak importe automatiquement ce fichier grace a :
```yaml
command:
  - start-dev
  - --import-realm
volumes:
  - ./docker/realm-export.json:/opt/keycloak/data/import/sma-realm-realm.json
```

Si le realm existe deja en base, `--import-realm` l'ignore (pas d'ecrasement). Il faut commiter ce fichier dans Git pour pouvoir reconstruire le realm sur une nouvelle machine.

---

### Roles definis dans le realm

| Role | Description | Attribue par defaut |
|------|-------------|:-------------------:|
| `USER` | Utilisateur standard | ✅ (tous les nouveaux comptes) |
| `ADMIN` | Administrateur complet | ❌ (a assigner manuellement) |
| `AUDITOR` | Auditeur RGPD — **lecture seule sur tout** (donnees, logs, audit) | ❌ (a assigner manuellement) |

> **Pourquoi AUDITOR = lecture seule et non tous les droits ?**
> Un auditeur independant doit pouvoir **tout lire** (acces aux preuves, aux logs, aux attentes RGPD) mais **jamais ecrire** : s'il pouvait modifier les donnees ou les logs, il pourrait falsifier les preuves qu'il est cense controler. C'est le principe fondamental de **l'independance de l'audit**. L'auditeur observe, il ne touche pas.

### 11.1 Roles metier (RBAC)

Le projet utilise une RBAC a 2 niveaux :

1. **Roles realm** (globaux) : portes par le JWT (`realm_access.roles`), consommes par Spring via `hasRole(...)` / `hasAnyRole(...)`.
2. **Roles client** (fins) : attaches aux clients `sma-monolithe` et `sma-thymeleaf-frontend`, composes dans les roles realm via `scripts/keycloak-realm.sh`.

Roles realm metier :

| Role realm | Usage metier | Droits principaux |
|---|---|---|
| `USER` | Utilisateur standard applicatif | Acces standard en lecture/ecriture sur son perimetre |
| `ADMIN` | Administration applicative | Tous les droits metier + gestion |
| `AUDITOR` | Audit/conformite | Lecture globale + export/analyse audit (pas d'ecriture metier) |

Baseline des roles client (creee/maintenue automatiquement par `./scripts/keycloak-realm.sh seed-demo-users`) :

| Client role | Attribue via | Finalite |
|---|---|---|
| `app-user` | `USER` | Acces aux fonctionnalites standard |
| `app-admin` | `ADMIN` | Administration applicative |
| `manage-users` | `ADMIN` | Gestion des utilisateurs applicatifs |
| `manage-settings` | `ADMIN` | Gestion de la configuration |
| `manage-reports` | `ADMIN` | Gestion des rapports |
| `app-auditor` | `AUDITOR` | Perimetre audit/conformite |
| `audit-read` | `AUDITOR` | Lecture des traces d'audit |
| `audit-export` | `AUDITOR` | Export des traces d'audit |
| `audit-analyze` | `AUDITOR` | Analyse des donnees d'audit |
| `audit-verify` | `AUDITOR` | Verification de conformite |

Regles de composition appliquees :

- `default-roles-sma-realm` inclut `USER` (nouveaux comptes = capacites minimales)
- `ADMIN` herite de `USER`
- les roles client ci-dessus sont composes dans `USER`, `ADMIN` ou `AUDITOR` selon la table precedente

### 11.2 Correction IDOR (lecture)

Objectif : eviter qu'un utilisateur authentifie puisse lire un objet qui ne lui appartient pas (Insecure Direct Object Reference).

Regle metier appliquee cote backend :

- `ADMIN` : acces global
- `USER` : acces restreint a ses propres donnees (verifiees par email JWT vs proprietaire de la ressource)
- `AUDITOR` : acces lecture selon regles endpoint/role (ex. clients/audit), sans ecriture

Etat de la correction par endpoint :

| Endpoint lecture | Etat IDOR | Comportement |
|---|---|---|
| `GET /queries/dossiers/{id}` | ✅ corrige | `USER` refuse (`403`) si email JWT != `dossier.client.mailClient` |
| `GET /queries/dossiers` | ✅ corrige | `USER` recoit uniquement ses dossiers |
| `GET /queries/vehicules/{id}` | ✅ corrige | `USER` refuse (`403`) si email JWT != `vehicule.client.mailClient` |
| `GET /queries/vehicules` | ✅ corrige | `USER` recoit uniquement ses vehicules |
| `GET /queries/documents/{id}` | ✅ corrige | `USER` refuse (`403`) si email JWT != `document.client.mailClient` |
| `GET /queries/documents` | ✅ corrige | `USER` recoit uniquement ses documents |
| `GET /queries/clients/{id}` | ✅ corrige | `USER` refuse (`403`) si email JWT != `client.mailClient` |
| `GET /queries/clients` | ✅ corrige | `USER` recoit uniquement ses clients |

Fichiers backend de reference :

- `backend/src/main/java/fr/cdrochon/smamonolithe/dossier/query/controllers/DossierQueryController.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/vehicule/query/controllers/VehiculeQueryController.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/document/query/controllers/DocumentQueryController.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/client/query/controllers/ClientQueryController.java`
- `backend/src/test/java/fr/cdrochon/smamonolithe/client/query/controllers/ClientQueryControllerTest.java`

---

### Script de gestion — `scripts/keycloak-realm.sh`

Ce script permet de gerer le realm sans jamais ouvrir l'interface Keycloak manuellement.

```bash
chmod +x scripts/keycloak-realm.sh

# Verifier que Keycloak et le realm sont operationnels
./scripts/keycloak-realm.sh status

# Exporter le realm depuis Keycloak (apres modification dans l'UI)
./scripts/keycloak-realm.sh backup
git add docker/realm-export.json && git commit -m "chore: backup realm"

# Recreer le realm sur une nouvelle machine (volume vide)
./scripts/keycloak-realm.sh restore

# Creer un utilisateur avec son role
./scripts/keycloak-realm.sh create-user

# Lister les utilisateurs
./scripts/keycloak-realm.sh list-users

# Assigner un role a un utilisateur existant
./scripts/keycloak-realm.sh add-role

# Creer automatiquement 3 comptes de demo (ADMIN/USER/AUDITOR)
./scripts/keycloak-realm.sh seed-demo-users
```

---

### Demarrage complet Keycloak (premiere fois ou nouvelle machine)

Keycloak est integre dans le `compose.yaml` principal. Il demarre automatiquement avec les autres services.

Une sauvegarde du compose est conservee dans `compose.yaml.bak` avant toute modification de stack.

Configuration principale deja incluse dans `compose.yaml`:

- Service `keycloak` sur l'image `quay.io/keycloak/keycloak:latest`
- Service `postgres-keycloak` dedie (port `5433`, volume `pg_keycloak`)
- Import automatique du realm via `--import-realm` + montage `./docker/realm-export.json`
- Credentials admin locaux par defaut: `admin` / `admin` via `KC_BOOTSTRAP_ADMIN_USERNAME` et `KC_BOOTSTRAP_ADMIN_PASSWORD` (a changer en environnement non-local)
- Backend configure avec `issuer-uri` et `jwk-set-uri` pour valider les JWT

```bash
# 1. Lancer toute la stack en mode standard
./scripts/dev.sh secure

# 2. Verifier l'etat du realm
./scripts/dev.sh realm-status

# 3. (Optionnel) reseeder les utilisateurs demo
./scripts/dev.sh seed-users

# 4. Sauvegarder le realm dans Git (si vous avez modifie sa config)
./scripts/keycloak-realm.sh backup
git add docker/realm-export.json && git commit -m "chore: backup realm"
```

---

### Reconstruction complete (volume perdu)

```bash
# Supprimer les conteneurs ET les volumes (reset total)
docker compose -f compose.yaml down -v

# Relancer avec le point d'entree officiel
./scripts/dev.sh secure

# Recreer les utilisateurs si necessaire
./scripts/dev.sh seed-users
```

> **Pourquoi les utilisateurs ne sont-ils pas dans le JSON ?**
> Par securite, les mots de passe (meme haches) ne sont pas exportes par defaut via `partial-export`.
> Pour inclure les utilisateurs, utilisez l'export complet depuis l'UI Keycloak :
> `Realm settings → Action → Export → cocher "Users"`.

---

### Eviter l'effet bloquant en developpement local

Commande prioritaire pour lancer l'appli facilement : `./scripts/dev.sh`.

Si vous ne voulez pas vous reconnecter en permanence pendant une session de dev:

- Gardez la meme session navigateur (cookies Keycloak valides)
- Utilisez les 3 comptes de demo (`seed-demo-users`) pour tester rapidement les roles
- En cas de besoin de debug rapide sans IAM, desactivez temporairement la securite via variables d'environnement

### Script unique de demarrage — `scripts/dev-up.sh`

Pour eviter de memoriser plusieurs commandes, utilisez le script unique:

```bash
chmod +x scripts/dev-up.sh

# Mode securise (Keycloak ON + seed automatique des 3 comptes demo)
./scripts/dev-up.sh secure

# Mode rapide (sans login sur frontend/backend)
./scripts/dev-up.sh fast

# Etat de la stack
./scripts/dev-up.sh status

# Arret
./scripts/dev-up.sh down

# Restart en mode choisi
./scripts/dev-up.sh restart-secure
./scripts/dev-up.sh restart-fast
```

Le mode `secure` prepare automatiquement les comptes suivants (s'ils n'existent pas):

- `admin-test / admin123!` (`ADMIN`)
- `user-test / user123!` (`USER`)
- `audit-test / audit123!` (`AUDITOR`)

Important:

- Le compte `admin / admin` sert a la console Keycloak (realm `master`).
- Les comptes applicatifs (`admin-test`, `user-test`, `audit-test`) se connectent au realm `sma-realm`.

Si vous souhaitez lancer le mode securise sans seed auto:

```bash
SKIP_SEED_USERS=true ./scripts/dev-up.sh secure
```

### Script d'acces rapide — `scripts/dev-login.sh`

Pour afficher directement les URLs utiles + credentials et ouvrir le navigateur:

```bash
chmod +x scripts/dev-login.sh

# Affiche URLs + credentials (mode par defaut)
./scripts/dev-login.sh

# Affiche + tente d'ouvrir frontend et Keycloak
./scripts/dev-login.sh open

# Affiche + check HTTP rapide (frontend/backend/keycloak)
./scripts/dev-login.sh check
```

Variables utiles:

```bash
# Ne pas ouvrir automatiquement le navigateur
NO_OPEN=true ./scripts/dev-login.sh open

# Surcharger une URL ou des credentials de demo
APP_URL=http://localhost:8091 KEYCLOAK_REALM=sma-realm ./scripts/dev-login.sh
KC_DEMO_ADMIN_PASSWORD='ChangeMe1!' KC_DEMO_USER_PASSWORD='ChangeMe2!' KC_DEMO_AUDITOR_PASSWORD='ChangeMe3!' ./scripts/dev-login.sh
```


Vous pouvez aussi surcharger les credentials de demo via variables d'environnement:

```bash
KC_DEMO_ADMIN_PASSWORD='ChangeMe1!' \
KC_DEMO_USER_PASSWORD='ChangeMe2!' \
KC_DEMO_AUDITOR_PASSWORD='ChangeMe3!' \
./scripts/keycloak-realm.sh seed-demo-users
```

---

### Variables d'environnement du script

```bash
export KEYCLOAK_BASE_URL="http://localhost:8080"   # defaut
export KEYCLOAK_ADMIN="admin"                       # defaut
export KEYCLOAK_ADMIN_PASSWORD="admin"              # defaut
export KEYCLOAK_REALM="sma-realm"                  # defaut
```

## 12. Pattern de creation CQRS/Axon (ce qui a ete implemente)

Cette section documente le pattern exact mis en place pour les creations `Dossier`, `Client`, `Vehicule`, `Garage`.

### 12.1 Probleme initial

Symptome observe:

- la commande est bien envoyee (visible cote Axon)
- l'evenement de creation Axon est emis
- mais l'API attend une confirmation de persistance qui n'arrive jamais
- resultat: timeout au bout de 20s (`BIZ_*_CREATE_FAILED`)

Cause technique:

- le `CommandService` attendait une `CompletableFuture`
- le `Query/EventHandlerService` persistait bien en base
- mais aucun signal robuste n'etait renvoye au `CommandService` apres commit DB

### 12.2 Pattern applique (flux de bout en bout)

Flux retenu:

```text
HTTP POST
  -> CommandController
  -> CommandService.create*(...) [cree une CompletableFuture par aggregateId]
  -> Axon CommandBus
  -> Aggregate (@CommandHandler -> apply Event)
  -> Query/EventHandlerService (@EventHandler)
       -> persistance JPA
       -> publication d'un Spring ApplicationEvent APRES COMMIT
  -> CommandService (@EventListener)
       -> complete*(dto)
       -> CompletableFuture.complete(dto)
  -> reponse HTTP sans timeout
```

Principe cle:

- le `CommandService` n'ecoute pas directement Axon pour la confirmation finale
- il ecoute un evenement Spring interne emis seulement apres persistance validee
- cela evite les dependances circulaires et stabilise le handshake commande/projection

### 12.3 Qui subscribe quoi (classes et handlers)

Cette partie est la plus importante pour comprendre le chainage.

#### Cote Axon (commandes -> evenements)

- **Aggregate command handler**: `@CommandHandler` dans les agregats (ex: `DossierAggregate`, `DocumentAggregate`, `VehiculeAggregate`)
- **Emission d'evenement Axon**: `AggregateLifecycle.apply(...)` pour `*CreatedEvent`
- **Bus utilise**: `CommandBus` / `EventBus` Axon via `CommandGateway`

#### Cote projection/persistance (subscribe Axon)

- `DossierEventHandlerService.on(DossierCreatedEvent)` (`@EventHandler`)
- `ClientEventHandlerService.on(ClientCreatedEvent)` (`@EventHandler`)
- `VehiculeEventHandlerService.on(VehiculeCreatedEvent)` (`@EventHandler`)
- `GarageEventHandlerService.on(GarageCreatedEvent)` (`@EventHandler`)

Role de ces handlers:

1. ils **consomment** l'evenement Axon (`subscribe` event processing)
2. ils **persistent** le read model en base (repositories JPA)
3. ils publient un **Spring ApplicationEvent** de confirmation (`*CreatedApplicationEvent`)

#### Cote retour commande (subscribe Spring)

- `DossierCommandService.onDossierCreatedApplicationEvent(...)` (`@EventListener`)
- `ClientCommandService.onClientCreatedApplicationEvent(...)` (`@EventListener`)
- `VehiculeCommandService.onVehiculeCreatedApplicationEvent(...)` (`@EventListener`)
- `GarageCommandService.onGarageCreatedApplicationEvent(...)` (`@EventListener`)

Role de ces listeners:

1. recuperer le DTO de confirmation
2. appeler `complete*Creation(...)`
3. terminer la `CompletableFuture` en attente

### 12.4 Sequence detaillee (subscribe Axon + handlers Spring)

```text
[1] HTTP POST /commands/*
    -> *CommandController

[2] *CommandService.create*(dto)
    -> cree future et l'indexe en map concurrente
    -> envoie *CreateCommand via CommandGateway

[3] *Aggregate (@CommandHandler)
    -> valide la commande
    -> apply(*CreatedEvent)

[4] *EventHandlerService (@EventHandler Axon)
    -> recoit l'evenement (subscribe event processor)
    -> persiste dans les repositories JPA
    -> publie *CreatedApplicationEvent (Spring)

[5] *CommandService (@EventListener Spring)
    -> recoit *CreatedApplicationEvent
    -> complete*Creation(...)
    -> future.complete(...)

[6] Controller
    -> retourne 201/OK sans timeout
```

### 12.5 Ce qui ne faut pas faire (anti-pattern)

- faire appeler directement un `CommandService` depuis un `@EventHandler` Axon (couplage fort)
- completer la future **avant** la persistance read model
- melanger confirmation metier et simple succes d'envoi `CommandGateway.send`

Le pattern choisi garantit que la commande n'est consideree "terminee" qu'apres persistance effective de la projection.

### 12.6 Ce qui a ete code exactement

1) **Wrappers de liste Axon pour les queries** (evite les erreurs de conversion `multipleInstancesOf`):

- `backend/src/main/java/fr/cdrochon/smamonolithe/dossier/query/dtos/DossierListResponse.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/client/query/dtos/ClientListResponse.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/dossier/query/dtos/GetAllDossiersDTO.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/client/query/dtos/GetAllClientsDTO.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/dossier/query/controllers/DossierQueryController.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/client/query/controllers/ClientQueryController.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/dossier/query/services/DossierEventHandlerService.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/client/query/services/ClientEventHandlerService.java`

2) **Evenements Spring de confirmation post-persistance**:

- `backend/src/main/java/fr/cdrochon/smamonolithe/dossier/query/events/DossierCreatedApplicationEvent.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/client/query/events/ClientCreatedApplicationEvent.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/vehicule/query/events/VehiculeCreatedApplicationEvent.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/garage/query/events/GarageCreatedApplicationEvent.java`

3) **Publication apres commit dans les EventHandlerService**:

- `backend/src/main/java/fr/cdrochon/smamonolithe/dossier/query/services/DossierEventHandlerService.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/client/query/services/ClientEventHandlerService.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/vehicule/query/services/VehiculeEventHandlerService.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/garage/query/services/GarageEventHandlerService.java`

4) **Completion asynchrone cote CommandService**:

- `backend/src/main/java/fr/cdrochon/smamonolithe/dossier/command/services/DossierCommandService.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/client/command/services/ClientCommandService.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/vehicule/command/services/VehiculeCommandService.java`
- `backend/src/main/java/fr/cdrochon/smamonolithe/garage/command/services/GarageCommandService.java`

Details communs implementes dans ces services:

- map concurrente `aggregateId -> CompletableFuture`
- `orTimeout(20s)` pour eviter l'attente infinie
- gestion d'erreur `commandGateway.send(...).whenComplete(...)`
- `@EventListener` Spring pour terminer la future sur confirmation post-commit

5) **Configuration Axon des processors**:

- `backend/src/main/java/fr/cdrochon/smamonolithe/infrastructure/AxonConfiguration.java`
- `backend/src/main/resources/application.properties`

Regle actuelle:

- mode global configure par `app.axon.default-event-processor-mode`
- valeur par defaut: `subscribing`
- passer a `tracking` seulement si l'environnement supporte correctement le token store Axon

### 12.7 Pourquoi on peut voir Commands/Queries mais pas Events dans Axon Server

Si `Commands` et `Queries` sont visibles mais pas les `Events`, verifier d'abord:

1. la connexion backend -> Axon Server est bien active
2. le stockage des events (Axon Server event store vs autre event store)
3. les logs d'event processing (erreurs SQL de token store)

Exemple de symptome deja rencontre:

- erreur SQL sur `token_entry` avec `for no key update`
- dans ce cas, les `TrackingEventProcessor` peuvent tomber en erreur
- impact: projection non alimentee ou comportement partiel selon le groupe

Mitigation appliquee ici:

- bascule par defaut en `subscribing` pour stabiliser la consommation d'evenements

### 12.8 Checklist de verification rapide

```bash
tail -f backend/logs/metier/business.log
```

```bash
tail -f backend/logs/technique/technical.log
```

```bash
grep -n "BIZ_.*_CREATE_REQUEST\|BIZ_.*_CREATED\|BIZ_.*_CREATE_CONFIRMED\|BIZ_.*_CREATE_FAILED" backend/logs/metier/business.log
```

Attendu pour une creation saine:

- `BIZ_*_CREATE_REQUEST`
- puis `BIZ_*_CREATED`
- puis `BIZ_*_CREATE_CONFIRMED`
- et **pas** de `BIZ_*_CREATE_FAILED`

