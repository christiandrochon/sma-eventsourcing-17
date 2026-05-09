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
- [9. Troubleshooting rapide](#9-troubleshooting-rapide)
- [10. Deployement Kubernetes (optionnel)](#10-deployement-kubernetes-optionnel)
- [11. Licence](#11-licence)

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

## 9. Troubleshooting rapide

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

## 10. Deployement Kubernetes (optionnel)

Les manifests sont disponibles dans `komp-smb/` pour un deploiement hors Docker Compose local.

## 11. Licence

Projet distribue sous licence MIT. Voir `LICENCE`.
