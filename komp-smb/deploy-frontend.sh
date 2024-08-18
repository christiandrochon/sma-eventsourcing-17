#!/bin/bash

# Naviguer à la racine du projet
cd "$(dirname "$0")/.."

# Temporarily set JAVA_HOME for using JDK 17.0.12 for Maven commands
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
PATH=$JAVA_HOME/bin:$PATH

# Naviguer dans le dossier thymeleaf-frontend
cd thymeleaf-frontend

# Construire le projet Maven et créer l'image Docker pour sma-monolithe
mvn clean package
docker build . -t sma-thymeleaf:v1.0

# Charger les images Docker dans Minikube
minikube image load sma-thymeleaf:v1.0

cd ..
# Appliquer les fichiers de déploiement Kubernetes
cd komp-smb
#kubectl apply -f sma-monolithe-deployment.yaml -n smb
kubectl apply -f thymeleaf-frontend-deployment.yaml -n smb
