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

## Tests automatisés (campagnes ajoutées)

Les campagnes de tests unitaires enrichies couvrent en priorité les domaines CQRS/Event Sourcing suivants :

- `client` : commandes, aggregate, handlers, mappers et cas limites
- `document` : commandes, aggregate, handlers, entités, mappers et cas limites
- `vehicule` : tests unitaires classiques + tests de cas limites
- `logging` : filtres techniques et gestionnaire global d’exceptions

Exemples de classes de test ajoutées/renforcées :

- `src/test/java/fr/cdrochon/smamonolithe/client/ClientEdgeCasesTest.java`
- `src/test/java/fr/cdrochon/smamonolithe/document/DocumentEdgeCasesTest.java`
- `src/test/java/fr/cdrochon/smamonolithe/vehicule/VehiculeClassicUnitTest.java`
- `src/test/java/fr/cdrochon/smamonolithe/vehicule/VehiculeEdgeCasesTest.java`
- `src/test/java/fr/cdrochon/smamonolithe/logging/TechnicalRequestWebFilterTest.java`
- `src/test/java/fr/cdrochon/smamonolithe/logging/GlobalTechnicalExceptionHandlerTest.java`

Lancer toute la suite :

```bash
mvn test
```

Lancer les suites principales ajoutées :

```bash
mvn -Dtest=Client*Test,Adresse*Test,Document*Test,Vehicule*Test,TechnicalRequestWebFilterTest,GlobalTechnicalExceptionHandlerTest,ClientWebConfigTest test
```

## Logs personnalisés business

Une séparation stricte des logs a été mise en place pour éviter que les événements métier soient noyés par les logs framework.

### Principe

- Les logs métier utilisent un logger dédié `BUSINESS`
- Les événements métier sont formatés avec des préfixes `BIZ_*`
- Les logs techniques restent séparés (`TECH_*`)
- La sortie métier est disponible en console et dans un fichier dédié

### Fichiers de configuration concernés

- `src/main/resources/logback-spring.xml`
- `src/main/resources/application.properties`
- `src/main/java/fr/cdrochon/smamonolithe/logging/BusinessLoggers.java`

### Emplacement du fichier métier

- `logs/business.log`

### Vérification rapide des logs business

1. Démarrer l’application
2. Appeler un endpoint de création (ex: `POST /commands/createClient`)
3. Vérifier la présence de lignes `BIZ_*` en console ou dans `logs/business.log`

Suivre le fichier en direct :

```bash
tail -f logs/business.log
```

## Clôture du lot logs techniques (5 items)

Ce lot est considéré clôturé quand les 5 points ci-dessous sont validés.

1. **Erreurs / exceptions centralisées**  
   Toutes les exceptions non gérées passent par un handler global avec log `TECH_EXCEPTION`.
   - Implémentation : `src/main/java/fr/cdrochon/smamonolithe/logging/GlobalTechnicalExceptionHandler.java`
   - Comportement attendu : log erreur + réponse HTTP 500 structurée (`timestamp`, `status`, `error`, `message`, `path`).
   - Validation : `src/test/java/fr/cdrochon/smamonolithe/logging/GlobalTechnicalExceptionHandlerTest.java`

2. **Latence HTTP et statut tracés**  
   Chaque requête backend produit un log `TECH_HTTP` avec méthode, path, status et durée.
   - Implémentation : `src/main/java/fr/cdrochon/smamonolithe/logging/TechnicalRequestWebFilter.java`
   - Comportement attendu : log `info` en succès, log `error` avec stacktrace en cas d'échec, statut et durée dans les deux cas.
   - Validation : `src/test/java/fr/cdrochon/smamonolithe/logging/TechnicalRequestWebFilterTest.java`

3. **Appels externes journalisés**  
   Les appels sortants (`RestTemplate` / `WebClient`) sont logués en technique avec statut, latence et erreur éventuelle.
   - Implémentation : `src/main/java/fr/cdrochon/smamonolithe/configuration/ClientWebConfig.java`
   - Comportement attendu :
     - `TECH_EXT_REST` / `TECH_EXT_REST_ERROR` pour `RestTemplate`
     - `TECH_EXT_WEBCLIENT` / `TECH_EXT_WEBCLIENT_ERROR` pour `WebClient`
   - Validation : `src/test/java/fr/cdrochon/smamonolithe/configuration/ClientWebConfigTest.java`

4. **Niveaux de logs maîtrisés (anti-bruit)**  
   Les frameworks restent en `WARN` et les logs utiles applicatifs/techniques sont conservés au bon niveau.
   - Configuration : `src/main/resources/application.properties`, `src/main/resources/logback-spring.xml`
   - Réglages clés : `logging.level.root=WARN`, `logging.level.org.springframework=WARN`, `logging.level.org.axonframework=WARN`, `logging.level.fr.cdrochon.smamonolithe.logging=INFO`.
   - Résultat : réduction du bruit framework, conservation des logs techniques pertinents.

5. **Validation par tests ciblés**  
   Les tests ciblés de la couche logging passent sur les composants techniques critiques :
   - `src/test/java/fr/cdrochon/smamonolithe/logging/TechnicalRequestWebFilterTest.java`
   - `src/test/java/fr/cdrochon/smamonolithe/logging/GlobalTechnicalExceptionHandlerTest.java`
   - `src/test/java/fr/cdrochon/smamonolithe/configuration/ClientWebConfigTest.java`
   - Commande de validation :

```bash
mvn -Dtest=TechnicalRequestWebFilterTest,GlobalTechnicalExceptionHandlerTest,ClientWebConfigTest test
```

## Licence

Ce projet est distribué sous licence MIT.
Voir le fichier [LICENSE](LICENSE).