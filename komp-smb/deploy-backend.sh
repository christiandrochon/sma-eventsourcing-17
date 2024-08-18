#!/bin/bash

# Naviguer à la racine du projet
cd "$(dirname "$0")/.."

# Construire le projet Maven et créer l'image Docker pour sma-monolithe
mvn clean package -DskipTests
docker build . -t sma-monolithe:v1.0

# charge l'image Docker dans Minikube
minikube image load sma-monolithe:v1.0

# Appliquer les fichiers de déploiement Kubernetes
cd komp-smb
kubectl apply -f sma-monolithe-deployment.yaml -n smb



