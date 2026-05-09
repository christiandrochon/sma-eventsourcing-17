# Scripts utilitaires

## `audit-check.sh`

Script de diagnostic rapide pour l'audit RGPD.

Ce qu'il verifie :

- disponibilite du conteneur PostgreSQL
- disponibilite du backend (`/actuator/health`)
- presence de la base `audit`
- presence des tables `audit_events`, `audit_expectations`, `audit_expectation_checks`
- presence du seed de la grille (15 attentes minimum)
- presence de preuves operationnelles (`audit_events` 30 jours)
- presence des triggers append-only

Mode standard :

```bash
./scripts/audit-check.sh
```

Mode remediatif (applique le schema d'audit) :

```bash
./scripts/audit-check.sh --apply-schema
```

Variables d'environnement supportees :

- `AUDIT_PG_CONTAINER` (defaut: `postgres-monolithe`)
- `AUDIT_BACKEND_URL` (defaut: `http://localhost:8092`)
- `AUDIT_POSTGRES_USER` (defaut: `postgres`)

## `audit-export.sh`

Script d'export de preuves pour un audit independant.

Fichiers generes :

- `audit_expectations_latest.csv`
- `audit_events_<N>d.csv`
- `audit_cross_garage_<N>d.csv`
- `audit_expectation_checks.csv`
- `README.txt` (manifest d'export)

Exemple :

```bash
./scripts/audit-export.sh
./scripts/audit-export.sh --days 90 --output-dir ./audit-exports
```

Variables d'environnement supportees :

- `AUDIT_PG_CONTAINER` (defaut: `postgres-monolithe`)
- `AUDIT_POSTGRES_USER` (defaut: `postgres`)
- `AUDIT_EXPORT_DAYS` (defaut: `30`)
- `AUDIT_EXPORT_DIR` (defaut: `./audit-exports`)

## `keycloak-token.sh`

Recupere un token Keycloak sans login interactif (utile pour scripts CI/CD ou operations repetitives).

Modes supportes :

- `client_credentials` (recommande pour service account)
- `password` (fallback legacy/dev)

Exemples :

```bash
# Service account
export KEYCLOAK_BASE_URL="http://localhost:8080"
export KEYCLOAK_REALM="sma-realm"
export KEYCLOAK_CLIENT_ID="sma-thymeleaf-frontend"
export KEYCLOAK_GRANT_TYPE="client_credentials"
./scripts/keycloak-token.sh

# Password grant (dev)
export KEYCLOAK_GRANT_TYPE="password"
export KEYCLOAK_USERNAME="my-user"
export KEYCLOAK_PASSWORD="my-password"
./scripts/keycloak-token.sh
```

