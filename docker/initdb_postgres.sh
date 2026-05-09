#!/bin/sh
set -e

echo "Init script: start"

: "${POSTGRES_USER:?POSTGRES_USER is required}"
: "${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is required}"
: "${POSTGRES_DB:?POSTGRES_DB is required}"

echo "Ensuring database '${POSTGRES_DB}' exists and granting privileges..."

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<EOSQL
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = '${POSTGRES_DB}') THEN
    EXECUTE format('CREATE DATABASE %I', '${POSTGRES_DB}');
  END IF;
END
\$\$;

-- le user POSTGRES_USER existe déjà (créé par l'image) si tu l'as fourni
GRANT ALL PRIVILEGES ON DATABASE "${POSTGRES_DB}" TO "${POSTGRES_USER}";
EOSQL

echo "Init script: monolithe database done"

# -----------------------------------------------------------------------
# BASE audit : traçabilité RGPD - non modifiable par l'applicatif
# -----------------------------------------------------------------------
echo "Creating 'audit' database..."

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<EOSQL
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'audit') THEN
    EXECUTE 'CREATE DATABASE audit';
  END IF;
END
\$\$;
GRANT ALL PRIVILEGES ON DATABASE audit TO "${POSTGRES_USER}";
EOSQL

echo "Creating audit schema and audit_events table..."

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname audit <<EOSQL
CREATE TABLE IF NOT EXISTS audit_events (
    id              BIGSERIAL                   PRIMARY KEY,
    event_time      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
    actor           VARCHAR(255)                NOT NULL,
    actor_garage    VARCHAR(255),
    action          VARCHAR(100)                NOT NULL,
    resource        VARCHAR(100)                NOT NULL,
    resource_id     VARCHAR(255),
    garage_id       VARCHAR(255),
    cross_garage    BOOLEAN                     NOT NULL DEFAULT FALSE,
    reason          TEXT,
    result          VARCHAR(50)                 NOT NULL,
    http_method     VARCHAR(10),
    http_path       VARCHAR(1024),
    http_status     INTEGER,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(512),
    details         TEXT
);

-- Index pour recherche RGPD : qui a fait quoi, quand
CREATE INDEX IF NOT EXISTS idx_audit_actor       ON audit_events (actor);
CREATE INDEX IF NOT EXISTS idx_audit_event_time  ON audit_events (event_time);
CREATE INDEX IF NOT EXISTS idx_audit_resource    ON audit_events (resource, resource_id);
CREATE INDEX IF NOT EXISTS idx_audit_garage      ON audit_events (garage_id);
CREATE INDEX IF NOT EXISTS idx_audit_cross       ON audit_events (cross_garage) WHERE cross_garage = TRUE;

-- SECURITE RGPD : l'applicatif ne peut qu'INSERer, jamais UPDATE ni DELETE
REVOKE UPDATE, DELETE ON audit_events FROM "${POSTGRES_USER}";
EOSQL

echo "Init script: audit database done"




##!/bin/bash
#set -e
#
## Wait for PostgreSQL to be ready
#until pg_isready -U "$POSTGRES_USER"; do
#  echo "Waiting for PostgreSQL to be ready..."
#  sleep 2
#done
#
##pour loger dans docker et executer le script =
##-> docker exec -it postgres-monolithe /bin/bash
##-> /docker-entrypoint-initdb.d/initdb_postgres.sh
##
##Connaitre le superuser
##-> SELECT usename FROM pg_user WHERE usesuper IS TRUE;
#
##Superutilisateur :
##-> psql -U postgres
##
##creer unsuperutilisateur (en etant connecté dejà comme superutilisateur)
##-> \du (verifier les roles existants et leurs attributs)
##-> CREATE ROLE postgres WITH SUPERUSER LOGIN;
##-> ALTER ROLE postgres WITH PASSWORD 'yourpassword'; (changer le mdp)
#
#echo "debut du script d'initialisation !!!!!"
#
## Vérifier que les variables d'environnement sont définies
#if [ -z "$POSTGRES_USER" ] || [ -z "$POSTGRES_PASSWORD" ]; then
#  echo "Les variables d'environnement POSTGRES_USER et POSTGRES_PASSWORD doivent être définies."
#  exit 1
#fi
## Check if the 'postgres' role exists
#if ! psql -U postgres -tc "SELECT 1 FROM pg_roles WHERE rolname = 'postgres'" | grep -q 1; then
#  echo "Creating 'postgres' role..."
#  psql -U postgres -c "CREATE ROLE postgres;"
#fi
#
## Check if environment variables are set
#if [ -z "$POSTGRES_USER" ] || [ -z "$POSTGRES_PASSWORD" ]; then
#  echo "Environment variables POSTGRES_USER and POSTGRES_PASSWORD must be set."
#  exit 1
#fi
#
## Retry logic for creating the PostgreSQL user
#for i in {1..5}; do
#  echo "Creating PostgreSQL user if it doesn't exist (attempt $i)..."
#  psql -U postgres -tc "SELECT 1 FROM pg_roles WHERE rolname = '${POSTGRES_USER}'" | grep -q 1 && break || \
#  psql -U postgres -c "CREATE USER ${POSTGRES_USER} WITH ENCRYPTED PASSWORD '${POSTGRES_PASSWORD}';" && break
#  sleep 2
#done
#
## Retry logic for creating the application database
#for i in {1..5}; do
#  echo "Creating application database if it doesn't exist (attempt $i)..."
#  psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = '${POSTGRES_DB}'" | grep -q 1 && break || \
#  psql -U postgres -c "CREATE DATABASE ${POSTGRES_DB} WITH OWNER ${POSTGRES_USER};" && break
#  sleep 2
#done
#
## Grant all privileges on the application database to the user
#echo "Granting privileges on application database..."
#psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE ${POSTGRES_DB} TO ${POSTGRES_USER};"
#
#echo "Script d'initialisation terminé."
#
###!/bin/bash
##set -e
##
### Wait for PostgreSQL to be ready
##until pg_isready -U "$POSTGRES_USER"; do
##  echo "Waiting for PostgreSQL to be ready..."
##  sleep 2
##done
##
#
### Create the user with a password for the application
##psql -U "$POSTGRES_USER" -c "CREATE USER ${POSTGRES_USER} WITH ENCRYPTED PASSWORD '${POSTGRES_PASSWORD}';"
### Create the database for the application if it doesn't exist
##psql -U "$POSTGRES_USER" -c "CREATE DATABASE ${POSTGRES_DB} WITH OWNER ${POSTGRES_USER};"
### Grant all privileges on the application database to the user
##psql -U "$POSTGRES_USER" -c "GRANT ALL PRIVILEGES ON DATABASE ${POSTGRES_DB} TO ${POSTGRES_USER};"
##
##
##
##
#
#
