#!/bin/bash
set -e

# Variables
POSTGRES_IMAGE="postgres:latest"
CONTAINER_NAME="my_postgres"
POSTGRES_PASSWORD="mysecretpassword"
ROLE_NAME="postgres"

# Pull the latest PostgreSQL image
docker pull $POSTGRES_IMAGE

# Start a new container from the PostgreSQL image
docker run --name $CONTAINER_NAME -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD -d $POSTGRES_IMAGE

# Wait for PostgreSQL to be ready
sleep 10

# Create the 'postgres' role inside the container
docker exec -it $CONTAINER_NAME psql -U postgres -c "CREATE ROLE $ROLE_NAME WITH LOGIN PASSWORD '$POSTGRES_PASSWORD' SUPERUSER;"
echo "Role '$ROLE_NAME' created."
