# Observability Baseline

## Metriques

Le backend expose Actuator, et l'endpoint `prometheus` est volontairement active pour l'observabilite.

Chaine standard:

```text
Spring Boot + Micrometer -> /actuator/prometheus -> Prometheus (scrape) -> Grafana (dashboards)
```

Concretement:

- `Micrometer` collecte les metriques JVM/applicatives cote application.
- `/actuator/prometheus` expose ces metriques au format attendu par Prometheus.
- Prometheus lit periodiquement cet endpoint.
- Grafana interroge Prometheus (pas directement l'application, dans le cas standard).

## Ce qui est configure dans ce projet

- Dependance metriques Prometheus: `backend/pom.xml`
  - `io.micrometer:micrometer-registry-prometheus`
- Exposition des endpoints en prod: `backend/src/main/resources/application-prod.properties`
  - `management.endpoints.web.exposure.include=${MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE:health,info,prometheus}`
- Exposition des endpoints en dev: `backend/src/main/resources/application-dev.properties`
  - `management.endpoints.web.exposure.include=${MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE:health,info,prometheus,metrics,conditions,beans,configprops,env,loggers,mappings,threaddump,heapdump,caches,httpexchanges}`

Endpoint principal:

```text
http://localhost:8092/actuator/prometheus
```

Important:

- `management.endpoints.web.exposure.include` remplace la liste d'endpoints exposes.
- Si vous mettez seulement `conditions`, vous n'exposez plus `prometheus`.
- Pour garder les metriques Prometheus tout en ajoutant l'autoconfiguration, utilisez par exemple:
  - `health,info,prometheus,conditions`

## Proprietes recommandees par profil

Objectif: garder une surface minimale en prod, et une surface plus riche en dev pour le debug.

- DEV (`backend/src/main/resources/application-dev.properties`)
  - `management.endpoints.web.exposure.include=health,info,prometheus,metrics,conditions,beans,configprops,env,loggers,mappings,threaddump,heapdump,caches,httpexchanges`
  - utile pour investiguer les auto-configurations, les composants Spring et les dumps JVM localement.

- PROD (`backend/src/main/resources/application-prod.properties`)
  - `management.endpoints.web.exposure.include=health,info,prometheus`
  - `management.endpoint.health.probes.enabled=true`
  - `metrics` et `conditions` doivent rester temporaires (debug ponctuel), puis etre retires.

## Activer ponctuellement `conditions`/`metrics` en Docker

Le backend en compose tourne avec le profil `prod`. Pour un debug court, surcharger via variable d'environnement:

```yaml
services:
  backend:
    environment:
      MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE: health,info,prometheus,metrics,conditions,beans,configprops,env,loggers,mappings,threaddump,heapdump,caches,httpexchanges
```

Puis revenir a la valeur minimale apres diagnostic.

## Securite: faut-il ouvrir ces endpoints en permanence ?

Non, pas recommande en production exposee publiquement.

- Risque: fuite d'informations internes (auto-configuration, metriques detaillees, composants)
- Bonne pratique: conserver `health,info,prometheus` en base
- Restreindre l'acces a `/actuator/prometheus` (reseau interne, allowlist ou auth)
- Ouvrir `conditions`/`metrics` seulement le temps du debug

## Catalogue des endpoints Actuator (ceux exposes en dev)

Base URL locale:

```text
http://localhost:8092/actuator
```

Resume ultra-court (qqs mots):

| Endpoint | Role |
|---|---|
| `/actuator/health` | Etat global appli |
| `/actuator/health/liveness` | Appli vivante ? |
| `/actuator/health/readiness` | Prete a servir ? |
| `/actuator/info` | Infos build/version |
| `/actuator/prometheus` | Metriques Prometheus |
| `/actuator/metrics` | Liste metriques |
| `/actuator/metrics/{name}` | Detail metrique |
| `/actuator/conditions` | Auto-config active/inactive |
| `/actuator/beans` | Beans Spring charges |
| `/actuator/configprops` | Proprietes configurees |
| `/actuator/env` | Variables/proprietes runtime |
| `/actuator/loggers` | Niveaux de logs |
| `/actuator/mappings` | Routes HTTP declarees |
| `/actuator/threaddump` | Etat des threads JVM |
| `/actuator/heapdump` | Snapshot memoire JVM |
| `/actuator/caches` | Etat des caches |
| `/actuator/httpexchanges` | Traces HTTP recentes |

Endpoints:

- `/actuator/health` : etat global
- `/actuator/health/liveness` : probe de vie
- `/actuator/health/readiness` : probe de readiness (si active)
- `/actuator/info` : metadonnees applicatives
- `/actuator/prometheus` : export metriques Prometheus
- `/actuator/metrics` : inventaire metriques
- `/actuator/metrics/{name}` : details d'une metrique
- `/actuator/conditions` : rapport d'auto-configuration Spring
- `/actuator/beans` : liste des beans Spring
- `/actuator/configprops` : proprietes de configuration resolues
- `/actuator/env` : environnement/proprietes
- `/actuator/loggers` : niveaux de logs (lecture/ajustement)
- `/actuator/mappings` : mappings HTTP exposes
- `/actuator/threaddump` : dump des threads JVM
- `/actuator/heapdump` : dump memoire JVM
- `/actuator/caches` : etat des caches
- `/actuator/httpexchanges` : historique des echanges HTTP (si collecte active)

Note:

- Certains endpoints peuvent rester absents selon les dependances/module charges.
- `heapdump` et `threaddump` peuvent etre volumineux et sensibles; limiter strictement leur exposition.

## Sante

- Liveness: `/actuator/health/liveness`
- Health global: `/actuator/health`

## Endpoints utiles (debug/ops)

- Metriques Prometheus: `/actuator/prometheus`
- Health global: `/actuator/health`
- Liveness probe: `/actuator/health/liveness`
- Conditions auto-config (si expose): `/actuator/conditions`
- Metriques JSON (si `metrics` expose): `/actuator/metrics`
- Beans Spring: `/actuator/beans`
- Loggers: `/actuator/loggers`
- Thread dump: `/actuator/threaddump`
- Heap dump: `/actuator/heapdump`

## Logs recommandés

- Backend technique: `backend/logs/technique/technical.log`
- Backend securite: `backend/logs/security.log`
- Frontend erreurs UI: `frontend/logs/technique/ui-error.log`

## Verification rapide

```bash
PROFILE=prod ./scripts/smoke-check.sh
curl -fsS http://localhost:8092/actuator/prometheus | head -n 20
curl -fsS http://localhost:8092/actuator/health | cat
curl -fsS http://localhost:8092/actuator/conditions | head -n 40
curl -fsS http://localhost:8092/actuator/beans | head -n 40
curl -fsS http://localhost:8092/actuator/loggers | head -n 40
curl -fsS http://localhost:8092/actuator/threaddump | head -n 40
curl -fsS http://localhost:8092/actuator/heapdump -o /tmp/backend-heapdump.hprof
```

