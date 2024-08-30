#!/bin/bash
set -e

# Wait for PostgreSQL to be ready
until pg_isready -U "$POSTGRES_USER" -h "localhost" -p 50006; do
  echo "Waiting for PostgreSQL to be ready..."
  sleep 2
done

# Create the database for the application if it doesn't exist
psql -U "$POSTGRES_USER" -h "localhost" -p 50006 -c "CREATE DATABASE ${POSTGRES_DB} WITH OWNER ${POSTGRES_USER};"

# Create the user with a password for the application
psql -U "$POSTGRES_USER" -h "localhost" -p 50006 -c "CREATE USER ${POSTGRES_USER} WITH ENCRYPTED PASSWORD '${POSTGRES_PASSWORD}';"

# Grant all privileges on the application database to the user
psql -U "$POSTGRES_USER" -h "localhost" -p 50006 -c "GRANT ALL PRIVILEGES ON DATABASE ${POSTGRES_DB} TO ${POSTGRES_USER};"

# Create the database for Keycloak if it doesn't exist
psql -U "$POSTGRES_USER" -h "localhost" -p 50006 -c "CREATE DATABASE ${KC_DB_URL_DATABASE} WITH OWNER ${KC_DB_USERNAME};"

# Create the user with a password for Keycloak
psql -U "$POSTGRES_USER" -h "localhost" -p 50006 -c "CREATE USER ${KC_DB_USERNAME} WITH ENCRYPTED PASSWORD '${KC_DB_PASSWORD}';"

# Grant all privileges on the Keycloak database to the user
psql -U "$POSTGRES_USER" -h "localhost" -p 50006 -c "GRANT ALL PRIVILEGES ON DATABASE ${KC_DB_URL_DATABASE} TO ${KC_DB_USERNAME};"
