#!/bin/bash
set -e

# Wait for PostgreSQL to be ready
until pg_isready -U "$POSTGRES_USER"; do
  echo "Waiting for PostgreSQL to be ready..."
  sleep 2
done

#pour loger dans docker et executer le script =
#-> docker exec -it postgres-monolithe /bin/bash
#-> /docker-entrypoint-initdb.d/initdb_postgres.sh
#
#Connaitre le superuser
#-> SELECT usename FROM pg_user WHERE usesuper IS TRUE;

#Superutilisateur :
#-> psql -U postgres
#
#creer unsuperutilisateur (en etant connecté dejà comme superutilisateur)
#-> \du (verifier les roles existants et leurs attributs)
#-> CREATE ROLE postgres WITH SUPERUSER LOGIN;
#-> ALTER ROLE postgres WITH PASSWORD 'yourpassword'; (changer le mdp)

echo "debut du script d'initialisation !!!!!"

# Vérifier que les variables d'environnement sont définies
if [ -z "$POSTGRES_USER" ] || [ -z "$POSTGRES_PASSWORD" ]; then
  echo "Les variables d'environnement POSTGRES_USER et POSTGRES_PASSWORD doivent être définies."
  exit 1
fi
# Check if the 'postgres' role exists
if ! psql -U postgres -tc "SELECT 1 FROM pg_roles WHERE rolname = 'postgres'" | grep -q 1; then
  echo "Creating 'postgres' role..."
  psql -U postgres -c "CREATE ROLE postgres;"
fi

# Check if environment variables are set
if [ -z "$POSTGRES_USER" ] || [ -z "$POSTGRES_PASSWORD" ]; then
  echo "Environment variables POSTGRES_USER and POSTGRES_PASSWORD must be set."
  exit 1
fi

# Retry logic for creating the PostgreSQL user
for i in {1..5}; do
  echo "Creating PostgreSQL user if it doesn't exist (attempt $i)..."
  psql -U postgres -tc "SELECT 1 FROM pg_roles WHERE rolname = '${POSTGRES_USER}'" | grep -q 1 && break || \
  psql -U postgres -c "CREATE USER ${POSTGRES_USER} WITH ENCRYPTED PASSWORD '${POSTGRES_PASSWORD}';" && break
  sleep 2
done

# Retry logic for creating the application database
for i in {1..5}; do
  echo "Creating application database if it doesn't exist (attempt $i)..."
  psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = '${POSTGRES_DB}'" | grep -q 1 && break || \
  psql -U postgres -c "CREATE DATABASE ${POSTGRES_DB} WITH OWNER ${POSTGRES_USER};" && break
  sleep 2
done

# Grant all privileges on the application database to the user
echo "Granting privileges on application database..."
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE ${POSTGRES_DB} TO ${POSTGRES_USER};"

echo "Script d'initialisation terminé."

##!/bin/bash
#set -e
#
## Wait for PostgreSQL to be ready
#until pg_isready -U "$POSTGRES_USER"; do
#  echo "Waiting for PostgreSQL to be ready..."
#  sleep 2
#done
#

## Create the user with a password for the application
#psql -U "$POSTGRES_USER" -c "CREATE USER ${POSTGRES_USER} WITH ENCRYPTED PASSWORD '${POSTGRES_PASSWORD}';"
## Create the database for the application if it doesn't exist
#psql -U "$POSTGRES_USER" -c "CREATE DATABASE ${POSTGRES_DB} WITH OWNER ${POSTGRES_USER};"
## Grant all privileges on the application database to the user
#psql -U "$POSTGRES_USER" -c "GRANT ALL PRIVILEGES ON DATABASE ${POSTGRES_DB} TO ${POSTGRES_USER};"
#
#
#
#


