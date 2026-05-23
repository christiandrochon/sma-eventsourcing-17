# Observability Baseline

## Metriques

Le backend expose Actuator. En prod, l'exposition inclut `prometheus` par defaut via:

- `management.endpoints.web.exposure.include=health,info,prometheus`

Endpoint:

```text
http://localhost:8092/actuator/prometheus
```

## Sante

- Liveness: `/actuator/health/liveness`
- Health global: `/actuator/health`

## Logs recommandés

- Backend technique: `backend/logs/technique/technical.log`
- Backend securite: `backend/logs/security.log`
- Frontend erreurs UI: `frontend/logs/technique/ui-error.log`

## Verification rapide

```bash
PROFILE=prod ./scripts/smoke-check.sh
curl -fsS http://localhost:8092/actuator/prometheus | head -n 20
```

