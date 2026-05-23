# Config Profiles Guide

## Regle de repartition

- `application.properties` : socle commun independant de l'environnement.
- `application-dev.properties` : valeurs locales (debug, localhost, bascules dev).
- `application-prod.properties` : valeurs prod (securite stricte, endpoints conteneur, logs sobres).

## Backend

- Commun: datasource parametree, flyway, axon global, logs, chemins Swagger.
- Dev: swagger actif, issuer localhost, axon localhost, securite permissive.
- Prod: swagger desactive par defaut, issuer keycloak conteneur, probes et logs prod.

## Frontend

- Commun: registration OAuth2, URL backend par variable, thymeleaf.
- Dev: provider OIDC localhost, logs debug reactif/json, auth optionnelle.
- Prod: provider OIDC docker, auth active.

