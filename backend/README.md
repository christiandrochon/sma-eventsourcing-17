Backend - instructions de développement
====================================

But: expliquer comment lancer le backend en dev et gérer les secrets locaux.

Fichiers importants
  - `backend/.env.template` : template de variables d'environnement pour le dev local. Copier en `backend/.env` (ne jamais committer `.env`).
  - `backend/.env` : fichier local gitignored contenant les secrets (POSTGRES_PASSWORD, etc.).
  - `scripts/dev-env.sh` : script recommandé pour lancer le backend en local (il charge `backend/.env` si présent puis lance `./mvnw`).

Procédure rapide
----------------
1) Copier l'exemple et remplir :

```bash
cp backend/.env.template backend/.env
# Editez backend/.env et définissez POSTGRES_PASSWORD et autres secrets
```

2) Rendre les scripts exécutables (si nécessaire) :

```bash
chmod +x scripts/dev.sh scripts/dev-up.sh scripts/dev-env.sh
```

3) Lancer la stack :

```bash
./scripts/dev.sh secure
```

Si vous souhaitez lancer uniquement le backend en local (sans la stack) :

```bash
./scripts/dev-env.sh
```

Sécurité et partage des secrets
------------------------------
- Ne commitez jamais `backend/.env` ni tout autre fichier contenant des secrets.
- Pour partager des secrets avec d'autres développeurs, utilisez un gestionnaire de secrets (Vault, secret manager Cloud) ou un canal sécurisé (par ex. credentials vault interne, ou outil d'ops).
 - Pour l'onboarding, fournissez soit :
   - un accès à Vault ou secret manager, ou
   - une procédure pour obtenir les secrets auprès de l'équipe Ops (ticket, email sécurisé).

Vault — résumé rapide
---------------------
- Vault (HashiCorp Vault) est un gestionnaire de secrets : il fournit stockage chiffré, policies d'accès, rotation et audit.
- Qui le fournit ? Typiquement l'équipe DevOps/Platform le déploie et le maintient. Vous ne le lancez pas vous-même pour la production (sauf si vous gérez l'infra).
- Modes d'utilisation :
  - Service managé / infra d'entreprise (souvent fourni par DevOps) — recommandé.
  - Déploiement self-hosted : Vault peut être lancé en tant que service sur VMs ou via Kubernetes (operator).
  - Pour développement local, on peut lancer Vault en `dev` mode (non sécurisé) pour tests.
- Intégration : Spring Cloud Vault, Vault Agent (sidecar), ou lecture manuelle via `vault` CLI.

Si vous voulez, nous pouvons automatiser la récupération des secrets depuis Vault (ex: script `scripts/get-secrets-from-vault.sh`) — dites-moi si vous voulez que je prépare cela.
