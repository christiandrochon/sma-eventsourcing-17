## Application de Maintenance Automobile (SMA)

### L'application SMA est une application de Maintenance Automobile.
  
> SMA offre une expérience utilisateur fluide et réactive, capable de traiter efficacement de grands volumes de données en temps réel, tout en maintenant des performances élevées.

> L'architecture de l'application permet une gestion dynamique des opérations, garantissant une réactivité optimale même sous forte charge.

> En matière de sécurité, SMA utilise des standards de pointe pour protéger les données et sécuriser les accès, assurant ainsi la confidentialité et l'intégrité des informations.

#### Caractéristiques principales :
   * __Programmation réactive__ : Optimisée pour gérer des charges élevées avec une faible latence grâce à l'approche asynchrone et non bloquante de _Spring WebFlux_ et de l'utilisation explicite de _futurs_.
   * __Event Sourcing et CQRS__ : Séparation claire des commandes et des requêtes, garantissant une meilleure évolutivité, une cohérence des données, et une traçabilité complète des événements.
   * __Sécurité__ : Utilisation de _OpenID Connect_ et du protocole _OAuth_ pour garantir une authentification et une autorisation robustes.
   * __DevOps et performance__ : Adoption des pratiques DevOps pour assurer des performances optimales, une haute disponibilité, une mise à l'échelle automatique, et une tolérance aux pannes, grâce à la répartition dynamique des charges entre les serveurs.

#### Déploiement et Infrastructure :
   * __Containerisation__ : L'application est entièrement dockerisée, facilitant son déploiement et sa gestion.
   * __Orchestration avec Kubernetes__ : Déploiement automatisé et gestion des conteneurs via _Kubernetes_, assurant la résilience et la scalabilité.
   * __Infrastructure Cloud__ : Hébergée sur _AWS_ avec des machines virtuelles pour une fiabilité maximale et une flexibilité à grande échelle. 

#### Accès à l'application :
   * Si vous avez téléchargé le code source depuis la branche 'master', à la racine du projet, executez 'docker compose -f compose.yaml up -d'. L'application sera accessible sur l'url 'http://localhost:8091'.
   * Si vous avez téléchargé le code source depuis la branche 'keycloak', l'application sera accessible  à l'url 'http://localhost:8094', mais vous aurez besoin de creer un compte depuis le formulaire d'authentification.
