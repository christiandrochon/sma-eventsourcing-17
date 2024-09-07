#!/bin/bash
set -e

# Wait for PostgreSQL to be ready
until pg_isready -U "$POSTGRES_USER"; do
  echo "Waiting for PostgreSQL to be ready..."
  sleep 2
done

echo "debut de ce putain script de merde !!!!!"


# 1
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


#### 2
## Créer l'utilisateur PostgreSQL
#echo "Création de l'utilisateur PostgreSQL si non existant..."
#psql -U postgres -tc "SELECT 1 FROM pg_roles WHERE rolname = '${POSTGRES_USER}'" | grep -q 1 || \
#psql -U postgres -c "CREATE USER ${POSTGRES_USER} WITH ENCRYPTED PASSWORD '${POSTGRES_PASSWORD}';"
#
## Check if the 'postgres' role exists
#if ! psql -U postgres -tc "SELECT 1 FROM pg_roles WHERE rolname = 'postgres'" | grep -q 1; then
#  echo "Creating 'postgres' role..."
#  psql -U postgres -c "CREATE ROLE postgres;"
#fi
## Create the database for the application if it doesn't exist
#echo "Création de la bd PostgreSQL..."
#psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = '${POSTGRES_DB}'" | grep -q 1 || \
#psql -U postgres -c "CREATE DATABASE ${POSTGRES_DB} WITH OWNER ${POSTGRES_USER};"
## Grant all privileges on the application database to the user
#echo "Création des privileges PostgreSQL..."
#psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE ${POSTGRES_DB} TO ${POSTGRES_USER};"





echo " creation du user keycloak"
# Check if the 'KC_DB_USERNAME' role exists
echo "Création du role keycloak..."
psql -U postgres -tc "SELECT 1 FROM pg_roles WHERE rolname = 'keycloak'" | grep -q 1 || \
psql -U postgres -c "CREATE USER keycloak WITH PASSWORD 'password';"
echo "Création de la bd keyclaok..."
psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = 'keycloak'" | grep -q 1 || \
psql -U postgres -c "CREATE DATABASE keycloak WITH OWNER keycloak;"
# Grant all privileges on the Keycloak database to the user
echo "Création des privileges keycloak..."
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak"

## Create the user with a password for Keycloak
#echo "Création de l'utilisateur keycloak..."
#psql -U postgres -tc "SELECT 1 FROM pg_roles WHERE rolname = '${KC_DB_USERNAME}'" | grep -q 1 || \
#psql -U postgres -c "CREATE USER ${KC_DB_USERNAME} WITH ENCRYPTED PASSWORD '${KC_DB_PASSWORD}';"
# Create the database for Keycloak if it doesn't exist
#echo "Création de la bd keyclaok..."
#psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = '${KC_DB_URL_DATABASE}'" | grep -q 1 || \
#psql -U postgres -c "CREATE DATABASE ${KC_DB_URL_DATABASE} WITH OWNER ${KC_DB_USERNAME};"
## Grant all privileges on the Keycloak database to the user
#echo "Création des privileges keycloak..."
#psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE ${KC_DB_URL_DATABASE} TO ${KC_DB_USERNAME};"


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
## Create the user with a password for Keycloak
#psql -U "$POSTGRES_USER" -c "CREATE USER ${KC_DB_USERNAME} WITH ENCRYPTED PASSWORD '${KC_DB_PASSWORD}';"
## Create the database for Keycloak if it doesn't exist
#psql -U "$POSTGRES_USER" -c "CREATE DATABASE ${KC_DB_URL_DATABASE} WITH OWNER ${KC_DB_USERNAME};"
## Grant all privileges on the Keycloak database to the user
#psql -U "$POSTGRES_USER" -c "GRANT ALL PRIVILEGES ON DATABASE ${KC_DB_URL_DATABASE} TO ${KC_DB_USERNAME};"
#
#
#
## Create the user with a password for the application
#psql -U "$POSTGRES_USER" -tc "SELECT 1 FROM pg_roles WHERE rolname = '${POSTGRES_USER}'" | grep -q 1 || \
#psql -U "$POSTGRES_USER" -c "CREATE USER ${POSTGRES_USER} WITH ENCRYPTED PASSWORD '${POSTGRES_PASSWORD}';"
#
#
## Create the database for the application if it doesn't exist
#psql -U "$POSTGRES_USER" -tc "SELECT 1 FROM pg_database WHERE datname = '${POSTGRES_DB}'" | grep -q 1 || \
#psql -U "$POSTGRES_USER" -c "CREATE DATABASE ${POSTGRES_DB} WITH OWNER ${POSTGRES_USER};"
#
## Create the user with a password for Keycloak
#psql -U "$POSTGRES_USER" -tc "SELECT 1 FROM pg_roles WHERE rolname = '${KC_DB_USERNAME}'" | grep -q 1 || \
#psql -U "$POSTGRES_USER" -c "CREATE USER ${KC_DB_USERNAME} WITH ENCRYPTED PASSWORD '${KC_DB_PASSWORD}';"
#
## Create the database for Keycloak if it doesn't exist
#psql -U "$POSTGRES_USER" -tc "SELECT 1 FROM pg_database WHERE datname = '${KC_DB_URL_DATABASE}'" | grep -q 1 || \
#psql -U "$POSTGRES_USER" -c "CREATE DATABASE ${KC_DB_URL_DATABASE} WITH OWNER ${KC_DB_USERNAME};"
#
## Grant all privileges on the application database to the user
#psql -U "$POSTGRES_USER" -c "GRANT ALL PRIVILEGES ON DATABASE ${POSTGRES_DB} TO ${POSTGRES_USER};"
#
## Grant all privileges on the Keycloak database to the user
#psql -U "$POSTGRES_USER" -c "GRANT ALL PRIVILEGES ON DATABASE ${KC_DB_URL_DATABASE} TO ${KC_DB_USERNAME};"

