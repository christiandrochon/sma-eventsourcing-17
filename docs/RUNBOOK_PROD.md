# Runbook Production

## 1) Verification rapide

```bash
./scripts/dev.sh status
./scripts/dev.sh check
PROFILE=prod ./scripts/smoke-check.sh
```

## 2) Sante backend

```bash
curl -fsS http://localhost:8092/actuator/health/liveness
curl -fsS http://localhost:8092/actuator/health
```

## 3) Logs essentiels

```bash
tail -f backend/logs/technique/technical.log
tail -f backend/logs/security.log
tail -f frontend/logs/technique/ui-error.log
```

## 4) Rollback simple (docker)

```bash
./scripts/dev.sh down
./scripts/dev.sh secure
```

## 5) Contrat API

```bash
./scripts/openapi-verify.sh canonical-only
# Option comparaison live:
./scripts/openapi-verify.sh compare-live
```

