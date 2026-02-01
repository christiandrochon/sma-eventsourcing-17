# SMA – Application de Maintenance Automobile

## Description
SMA est une application intranet de maintenance automobile développée en Java avec Spring Boot.
Elle repose sur une architecture orientée événements et une approche réactive côté backend.

Le projet est entièrement exécutable en local via Docker Compose.
Les images du backend et du frontend sont construites à la volée à partir des Dockerfile présents dans le dépôt.

## Architecture
- Backend : Spring Boot (WebFlux)
- Frontend : Thymeleaf (rendu côté serveur)
- Build : Maven
- Base de données : PostgreSQL
- Infrastructure locale : Docker / Docker Compose

Les patterns **CQRS** et **Event Sourcing** sont utilisés pour séparer les responsabilités
de commande et de lecture, améliorer la traçabilité des changements et faciliter l’évolution du modèle métier.

## Sécurité
L’authentification et l’autorisation reposent sur les standards :
- OAuth 2.0
- OpenID Connect

(L’implémentation dépend de la configuration de l’environnement d’exécution.)

## Structure du projet
- Backend Spring Boot : racine du projet
- Frontend Thymeleaf : `thymeleaf-frontend`
- Données d’exemple : `thymeleaf-frontend/src/main/resources/vehicules.json`
- Fichier Docker Compose : `compose.yaml`
- Dockerfile backend : `Dockerfile`
- Dockerfile frontend : `thymeleaf-frontend/Dockerfile`

## Backend (Spring Boot WebFlux)
Le backend Spring Boot constitue le cœur applicatif et expose les endpoints nécessaires
au rendu serveur des vues Thymeleaf ainsi qu’aux opérations métier.

## Frontend (Thymeleaf)

Le frontend de l’application est entièrement développé avec **Thymeleaf** et repose sur
un rendu **côté serveur** des vues HTML.

Cette approche permet :

- la génération dynamique des pages HTML côté backend
- le contrôle complet des champs de formulaire (binding, validation, affichage conditionnel)
- l’intégration native avec Spring (modèle, sécurité, internationalisation)
- la centralisation de la logique de présentation côté serveur
- la réduction de la logique JavaScript côté client

Les vues Thymeleaf sont directement liées au modèle métier exposé par le backend,
ce qui garantit la cohérence des données affichées et simplifie la maintenance
dans un contexte applicatif intranet.

## Accès (local)

- Frontend (Thymeleaf) : http://localhost:8091
- Backend (API / Actuator) : http://localhost:8092
- Healthcheck backend : http://localhost:8092/actuator/health
- Axon Server (dashboard) : http://localhost:8024
- pgAdmin : http://localhost:6002
- PostgreSQL : localhost:5432


## Lancement de l’application (environnement local)

À la racine du projet :

```bash
docker compose -f compose.yaml up -d
```
## Profils Spring

L’application utilise les profils Spring pour distinguer les configurations
selon l’environnement d’exécution.

- `default` : configuration par défaut (`application.properties`)
- `local` : exécution locale hors Docker
- `prod` : exécution via Docker Compose (intranet)

Le profil actif est défini via la variable d’environnement `SPRING_PROFILES_ACTIVE`.
Dans l’environnement Docker, le profil `prod` est utilisé.

## Licence

Ce projet est distribué sous licence MIT.
Voir le fichier [LICENSE](LICENSE).