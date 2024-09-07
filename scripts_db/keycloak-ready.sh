#!/bin/bash

# Path to the configuration file
CONFIG_FILE="conf/keycloak.conf"

# Check if the configuration file exists
if [ ! -f "$CONFIG_FILE" ]; then
  echo "Configuration file not found: $CONFIG_FILE"
  exit 1
fi

# Source the configuration file
source "$CONFIG_FILE"

# Maximum number of retries
MAX_RETRIES=30

# Interval between retries in seconds
RETRY_INTERVAL=10

# Function to check if Keycloak is ready
check_keycloak_ready() {
  curl -f http://localhost:8081/realms/sma-realm > /dev/null 2>&1
}

# Retry loop
for ((i=1; i<=MAX_RETRIES; i++)); do
  if check_keycloak_ready; then
    echo "Keycloak is ready."
    exit 0
  fi
  echo "Waiting for Keycloak to become ready... ($i/$MAX_RETRIES)"
  sleep $RETRY_INTERVAL
done

echo "Keycloak did not become ready in time."
exit 1
