#!/bin/bash

# Naviguer à la racine du projet
cd "$(dirname "$0")/.."

# Le compilateur a besoin de la version 17 alors que mon systeme est defini sur la version 8
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
PATH=$JAVA_HOME/bin:$PATH

# Naviguer dans le dossier thymeleaf-frontend
cd thymeleaf-frontend

# Construire le projet Maven et créer l'image Docker pour sma-thymeleaf
mvn clean package
docker build --no-cache . -t btrurqnt/sma-thymeleaf:v1.1
docker tag sma-thymeleaf:v1.1 btrurqnt/sma-thymeleaf:v1.1

echo $DOCKER_PASSWORD | docker login -u btrurqnt --password-stdin
docker push btrurqnt/sma-thymeleaf:v1.1

## Arrêter tous les conteneurs utilisant l'image sma-thymeleaf
#docker ps -q --filter ancestor=btrurqnt/sma-thymeleaf:v1.1 | xargs -r docker stop
## Supprimer tous les conteneurs utilisant l'image sma-thymeleaf
#docker ps -a -q --filter ancestor=btrurqnt/sma-thymeleaf:v1.1 | xargs -r docker rm
## Supprimer toutes les images sma-thymeleaf
#docker images -q btrurqnt/sma-thymeleaf:v1.1 | xargs -r docker rmi -f
## En cas de changement du code source via la jar, suppression de l'ancienne image
minikube ssh -- docker image rmi -f btrurqnt/sma-thymeleaf:v1.1

minikube image load btrurqnt/sma-thymeleaf:v1.1

cd ..
# Appliquer les fichiers de déploiement Kubernetes
cd komp-smb
kubectl apply -f thymeleaf-frontend-deployment.yaml -n sma
