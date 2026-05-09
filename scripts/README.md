# Scripts utilitaires

## `dev.sh` (point d'entree unique recommande)

Facade simple pour lancer et piloter l'application locale sans memoriser plusieurs scripts.

Exemples :

```bash
./scripts/dev.sh secure
./scripts/dev.sh fast
./scripts/dev.sh open
./scripts/dev.sh check
./scripts/dev.sh seed-users
./scripts/dev.sh ensure-users
./scripts/dev.sh down
```

Ce script delegue vers :

- `dev-up.sh` (start/stop/restart secure|fast)
- `dev-login.sh` (show/open/check URLs + credentials)
- `keycloak-realm.sh` (`seed-demo-users`, `status`)

Decision rapide (quel script selon mon objectif) :

| Objectif | Script/commande |
|---|---|
| Lancer la stack securisee | `./scripts/dev.sh secure` |
| Lancer la stack sans login (debug) | `./scripts/dev.sh fast` |
| Afficher URLs + credentials | `./scripts/dev.sh show` |
| Ouvrir directement app + Keycloak | `./scripts/dev.sh open` |
| Verifier la disponibilite HTTP | `./scripts/dev.sh check` |
| Creer les 3 comptes demo RBAC | `./scripts/dev.sh seed-users` |
| Garantir les users avant login (alias explicite) | `./scripts/dev.sh ensure-users` |
| Verifier le realm Keycloak | `./scripts/dev.sh realm-status` |
| Arreter la stack | `./scripts/dev.sh down` |

## `dev-up.sh`

Gestion du cycle de vie local de la stack (Docker Compose) : mode securise avec Keycloak ou mode rapide sans login.

Exemples :

```bash
./scripts/dev-up.sh secure
./scripts/dev-up.sh fast
./scripts/dev-up.sh status
./scripts/dev-up.sh down
```

## `dev-login.sh`

Affiche les URLs utiles et credentials de demo; peut ouvrir automatiquement le navigateur et faire un check HTTP rapide.

Exemples :

```bash
./scripts/dev-login.sh
./scripts/dev-login.sh open
./scripts/dev-login.sh check
```

## `keycloak-realm.sh`

Gestion directe du realm Keycloak (hors facade `dev.sh`).

Exemples :

```bash
./scripts/keycloak-realm.sh status
./scripts/keycloak-realm.sh seed-demo-users
./scripts/keycloak-realm.sh create-user
./scripts/keycloak-realm.sh add-role
./scripts/keycloak-realm.sh backup
./scripts/keycloak-realm.sh restore
```

Comptes demo crees par `seed-demo-users` (si absents) :

- `admin-test / admin123!` (`ADMIN`)
- `user-test / user123!` (`USER`)
- `audit-test / audit123!` (`AUDITOR`)

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

Notes RBAC audit :

- Les endpoints backend `/audit/compliance/**` attendent un role `ADMIN` ou `AUDITOR` (quand `APP_SECURITY_ENABLED=true`).
- Le token recupere doit contenir un role lecture (`ADMIN`/`AUDITOR`) pour les GET.
- L'ecriture (`POST /audit/compliance/**`) est reservee par defaut a `ADMIN`.
