#!/bin/bash

set -e

# Variables d'environnement
ROLE_NAME="${ROLE_NAME:-myuser}"
ROLE_PASSWORD="${ROLE_PASSWORD:-mypassword}"
DATABASE_NAME="${DATABASE_NAME:-mydatabase}"
PGUSER="${PGUSER:-adminuser}"
PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5432}"

# Afficher un message d'information
echo "Début du script d'initialisation de la base de données..."

# Créer le rôle avec les droits de superutilisateur (si l'utilisateur courant a les droits nécessaires)
echo "Création du rôle \"$ROLE_NAME\"..."
psql -U "$PGUSER" -h "$PGHOST" -p "$PGPORT" -c "DO \$\$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_roles
        WHERE rolname = '$ROLE_NAME'
    ) THEN
        EXECUTE 'CREATE ROLE $ROLE_NAME WITH LOGIN PASSWORD '''$ROLE_PASSWORD''' SUPERUSER';
        RAISE NOTICE 'Le rôle \"$ROLE_NAME\" a été créé avec succès.';
    ELSE
        RAISE NOTICE 'Le rôle \"$ROLE_NAME\" existe déjà.';
    END IF;
END \$\$;"

# Créer la base de données
echo "Création de la base de données \"$DATABASE_NAME\"..."
psql -U "$PGUSER" -h "$PGHOST" -p "$PGPORT" -c "DO \$\$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_database
        WHERE datname = '$DATABASE_NAME'
    ) THEN
        EXECUTE 'CREATE DATABASE $DATABASE_NAME OWNER $ROLE_NAME';
        RAISE NOTICE 'La base de données \"$DATABASE_NAME\" a été créée avec succès.';
    ELSE
        RAISE NOTICE 'La base de données \"$DATABASE_NAME\" existe déjà.';
    END IF;
END \$\$;"

# Afficher un message de fin
echo "Script terminé."




##!/bin/bash
#set -e
#
## Wait for PostgreSQL to be ready
#until pg_isready -U "$POSTGRES_USER"; do
#  echo "Waiting for PostgreSQL to be ready..."
#  sleep 2
#done
#
#echo "creation du role myrole..."
#psql  -c "CREATE ROLE myrole WITH LOGIN PASSWORD 'password';"
#echo "creation du role myadmin..."
#psql -c "CREATE ROLE myadmin WITH LOGIN PASSWORD 'password' CREATEDB CREATEROLE;"
#echo "creation du role superuser..."
#psql -c "CREATE ROLE superuser WITH LOGIN PASSWORD 'password' SUPERUSER;"
#echo "accordé la bd au role du role myuser..."
#psql  -c "GRANT CONNECT ON DATABASE monolithe TO myrole;"
#echo "accordé la bd au role du role superuser..."
#psql -c "ALTER ROLE myrole WITH SUPERUSER;"
#
#
#
## Create the database for the application if it doesn't exist
#echo "Création de la bd monolithe..."
#psql -U "postgres" -c "CREATE DATABASE monolithe WITH OWNER postgres;"
#
## Create the user with a password for the application
#echo "Création de l'utilisateur postgres..."
#psql -U "postgres" -c "CREATE USER postgres WITH ENCRYPTED PASSWORD 'password';"
#
## Grant all privileges on the application database to the user
#echo "Création des privileges monolithe..."
#psql -U "postgres" -c "GRANT ALL PRIVILEGES ON DATABASE monolithe TO postgres;"
#
#echo "Script d'initialisation terminé."





##!/bin/bash
#
#set -e
#
## Variables d'environnement
#ROLE_NAME="${ROLE_NAME:-postgres}"
#ROLE_PASSWORD="${ROLE_PASSWORD:-password}"
#DATABASE_NAME="${DATABASE_NAME:-monolithe}"
#PGUSER="${PGUSER:-postgres}"
#PGHOST="${PGHOST:-postgres-monolithe}"
#PGPORT="${PGPORT:-5432}"
#
## Afficher un message d'information
#echo "Début du script d'initialisation de la base de données..."
#
## Créer le rôle 'postgres' avec les droits de superutilisateur
#echo "Création du rôle superutilisateur \"$ROLE_NAME\"..."
#psql -U "$PGUSER" -h "$PGHOST" -p "$PGPORT" -c "DO \$\$
#BEGIN
#    IF NOT EXISTS (
#        SELECT 1
#        FROM pg_roles
#        WHERE rolname = '$ROLE_NAME'
#    ) THEN
#        EXECUTE 'CREATE ROLE $ROLE_NAME WITH LOGIN PASSWORD '''$ROLE_PASSWORD''' SUPERUSER';
#        RAISE NOTICE 'Le rôle \"$ROLE_NAME\" a été créé avec succès.';
#    ELSE
#        RAISE NOTICE 'Le rôle \"$ROLE_NAME\" existe déjà.';
#    END IF;
#END \$\$;"
#
## Créer la base de données 'monolithe'
#echo "Création de la base de données \"$DATABASE_NAME\"..."
#psql -U "$PGUSER" -h "$PGHOST" -p "$PGPORT" -c "DO \$\$
#BEGIN
#    IF NOT EXISTS (
#        SELECT 1
#        FROM pg_database
#        WHERE datname = '$DATABASE_NAME'
#    ) THEN
#        EXECUTE 'CREATE DATABASE $DATABASE_NAME OWNER $ROLE_NAME';
#        RAISE NOTICE 'La base de données \"$DATABASE_NAME\" a été créée avec succès.';
#    ELSE
#        RAISE NOTICE 'La base de données \"$DATABASE_NAME\" existe déjà.';
#    END IF;
#END \$\$;"
#
## Afficher un message de fin
#echo "Script terminé."
