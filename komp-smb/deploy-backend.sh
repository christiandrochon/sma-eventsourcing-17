#!/bin/bash

# Naviguer à la racine du projet
cd "$(dirname "$0")/.."

mvn clean package -DskipTests
docker build --no-cache . -t btrurqnt/sma-monolithe:v1.1
docker tag sma-monolithe:v1.1 btrurqnt/sma-monolithe:v1.1

echo $DOCKER_PASSWORD | docker login -u btrurqnt --password-stdin
docker push btrurqnt/sma-monolithe:v1.1

# Arrêter et supprimer les conteneurs utilisant l'image
#docker ps -q --filter ancestor=btrurqnt/sma-monolithe:v1.1 | xargs -r docker stop
#docker ps -a -q --filter ancestor=btrurqnt/sma-monolithe:v1.1 | xargs -r docker rm
# En cas de changement du code source via la jar, suppression de l'ancienne image
#minikube ssh -- docker image rmi -f btrurqnt/sma-monolithe:v1.1

minikube image load btrurqnt/sma-monolithe:v1.1

# Déploiement
cd komp-smb
kubectl apply -f sma-monolithe-deployment.yaml -n sma



