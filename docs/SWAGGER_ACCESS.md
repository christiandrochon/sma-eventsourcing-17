# Swagger Access Policy

## Objectif

Rendre la documentation API accessible en dev sans ouvrir les APIs metier, et la desactiver par defaut en prod.

## Regles

- En dev: Swagger UI et /v3/api-docs sont accessibles sans token si `app.security.swagger-public=true`.
- En prod: Swagger est desactive par defaut via `springdoc.swagger-ui.enabled=false` et `springdoc.api-docs.enabled=false`.

## Chemins

- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs/**`

