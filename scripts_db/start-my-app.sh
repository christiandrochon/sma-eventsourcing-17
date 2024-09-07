#!/bin/bash

# Wait for Keycloak to be ready
./wait-for-it.sh keycloak 8081 --timeout=60

# Check if Keycloak is ready
until curl --output /dev/null --silent --head --fail http://keycloak:8081/realms/sma-realm; do
  echo 'Waiting for Keycloak to be fully available...'
  sleep 5
done

# Start the application
echo 'Starting the application...'
# Add your application start command here




##!/bin/bash
#
#set -e
#
## Variables d'environnement
#HOST="${KEYCLOAK_HOST:-keycloak}"
#PORT="${KEYCLOAK_PORT:-8081}"
#
#echo "Waiting for Keycloak at $HOST:$PORT to be available..."
#
## Attente jusqu'à ce que Keycloak soit opérationnel
#until curl -sSf "http://localhost:8081/realms/sma-realm" > /dev/null; do
#  >&2 echo "Keycloak is unavailable - sleeping"
#  sleep 5
#done
#
#>&2 echo "Keycloak is up - starting application"
#
## Lancer votre application ici
#exec "$@"
