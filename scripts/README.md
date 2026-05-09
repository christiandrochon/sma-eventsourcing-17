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

