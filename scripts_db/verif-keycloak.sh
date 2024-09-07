#!/bin/bash

# Attendre que PostgreSQL soit prêt
echo "Attente que PostgreSQL soit prêt..."
#/opt/keycloak/wait-for-it.sh postgres-monolithe:5432 --timeout=60 -- echo "PostgreSQL est prêt."
sleep 60
# Démarrer Keycloak en mode développement
#echo "Démarrage de Keycloak..."
#/opt/keycloak/bin/kc.sh start-dev


#echo "attendre keycloak"
#./wait-for-it.sh keycloak:8081 -- echo "Keycloak is up"
