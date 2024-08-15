#!/bin/bash
set -e

# Wait for PostgreSQL to be ready
until pg_isready -U "$POSTGRES_USER"; do
  echo "Waiting for PostgreSQL to be ready..."
  sleep 2
done

# Create the database if it doesn't exist
psql -U "$POSTGRES_USER" -c "CREATE DATABASE ${POSTGRES_DB} WITH OWNER ${POSTGRES_USER};"

# Create the user with a password
psql -U "$POSTGRES_USER" -c "CREATE USER ${POSTGRES_USER} WITH ENCRYPTED PASSWORD '${POSTGRES_PASSWORD}';"

# Grant all privileges on the database to the user
psql -U "$POSTGRES_USER" -c "GRANT ALL PRIVILEGES ON DATABASE ${POSTGRES_DB} TO ${POSTGRES_USER};"

#psql -U keycloak -c "create database monolithe"







