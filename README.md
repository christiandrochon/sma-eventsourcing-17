# SMA - Application de Maintenance Automobile

Application intranet de maintenance automobile basee sur Spring Boot, avec separation claire entre interface serveur (Thymeleaf) et coeur metier CQRS/Event Sourcing.

## Sommaire

- [1. Vue d'ensemble](#1-vue-densemble)
- [2. Architecture actuelle](#2-architecture-actuelle)
  - [2.1 Runtime local (Docker Compose)](#21-runtime-local-docker-compose)
  - [2.2 Structure du depot](#22-structure-du-depot)
  - [2.3 Flux CQRS simplifie](#23-flux-cqrs-simplifie)
- [3. Stack technique](#3-stack-technique)
- [4. Structure des modules](#4-structure-des-modules)
- [5. Demarrage rapide](#5-demarrage-rapide)
- [5.1 Demarrage Docker Compose](#51-demarrage-docker-compose)
- [5.2 Demarrage local (sans conteneur app)](#52-demarrage-local-sans-conteneur-app)
- [6. Build et tests](#6-build-et-tests)
- [7. Configuration et profils Spring](#7-configuration-et-profils-spring)
- [8. Logs](#8-logs)
- [9. Grille d'audit independant (RGPD + gouvernance data)](#9-grille-daudit-independant-rgpd--gouvernance-data)
- [10. Troubleshooting rapide](#10-troubleshooting-rapide)
- [11. Deployement Kubernetes (optionnel)](#11-deployement-kubernetes-optionnel)
- [12. Licence](#12-licence)

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

### 5.1 Demarrage Docker Compose

Depuis la racine du projet :

```bash
docker compose -f compose.yaml up -d
```

Arret de la stack :

```bash
docker compose -f compose.yaml down
```

Voir les logs des services :

```bash
docker compose -f compose.yaml logs -f frontend
docker compose -f compose.yaml logs -f backend
```

Rebuild complet des images :

```bash
docker compose -f compose.yaml up -d --build
```

### 5.2 Demarrage local (sans conteneur app)

Option pratique : lancer seulement les dependances (PostgreSQL + Axon + pgAdmin) en Docker, puis demarrer les apps en local.

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

4. **Aucun log business visible**
   - verifier `logging.file.path` dans les fichiers `application-*.properties`
   - verifier la config de `backend/src/main/resources/logback-spring.xml`

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

## 11. Deployement Kubernetes (optionnel)

Les manifests sont disponibles dans `komp-smb/` pour un deploiement hors Docker Compose local.

## 12. Licence

Projet distribue sous licence MIT. Voir `LICENCE`.
