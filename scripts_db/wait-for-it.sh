#!/bin/bash

# Wait for the specified host and port to be available
HOST=$1
PORT=$2
TIMEOUT=${3:-60}

echo "Waiting for $HOST:$PORT to be available..."

for ((i=1; i<=TIMEOUT; i++)); do
  if nc -z $HOST $PORT; then
    echo "$HOST:$PORT is available."
    exit 0
  fi
  echo "Waiting for $HOST:$PORT... ($i/$TIMEOUT)"
  sleep 1
done

echo "$HOST:$PORT did not become available in time."
exit 1






#./wait-for-it.sh keycloak:8081 --timeout=60 -- echo "Keycloak port is open"
#
## Now, check if Keycloak is really ready by pinging its health or realm endpoint
#until $(curl --output /dev/null --silent --head --fail http://keycloak:8081/realms/sma-realm); do
#    echo "Waiting for Keycloak to be fully available..."
#    sleep 5
#done
#
#echo "Keycloak is ready!"
## Now you can start your application
#./start-my-app.sh

