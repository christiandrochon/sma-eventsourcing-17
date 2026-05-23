# OpenAPI versionnee

Ce dossier contient la specification OpenAPI canonique du projet quand on souhaite versionner le contrat d'API.

## Regle de projet

- Exports locaux (racine): `openapi.json` / `openapi.yaml` -> ignores par Git.
- Version canonique: `docs/openapi/openapi.json` (et optionnellement `docs/openapi/openapi.yaml`) -> versionnes.

## Generation

Depuis la racine du projet:

```bash
chmod +x scripts/export-openapi.sh
./scripts/export-openapi.sh --mode canonical
```

Par defaut, le script cible `http://localhost:8092/v3/api-docs`.

