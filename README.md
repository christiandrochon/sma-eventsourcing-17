## Application de Maintenance Automobile (SMA)

  
> #### L'application SMA est une solution réactive basée sur Spring WebFlux qui exploite pleinement les avantages des CompletableFuture pour des traitements asynchrones et non-bloquants. Elle implémente les patterns d'architecture Event Sourcing et CQRS, offrant ainsi une grande évolutivité, une résilience accrue, et une maintenance simplifiée.<br><br>
  


Caractéristiques principales :
  * __Architecture réactive__ : Conçue pour gérer des charges élevées avec une faible latence, grâce à l'approche non-bloquante de _Spring WebFlux_.
  * __Event Sourcing et CQRS__ : Séparation claire des commandes et des requêtes pour une meilleure évolutivité et cohérence des données, avec une traçabilité complète des événements.
  * __DevOps et performance__ : Intégration des pratiques DevOps pour assurer des performances optimales, une haute disponibilité, une mise à l'échelle automatique et une tolérance aux pannes grâce à la répartition dynamique des charges entre les serveurs.

Déploiement et Infrastructure :
   * __Containerisation__ : L'application est entièrement dockerisée, facilitant son déploiement et sa gestion.
   * __Orchestration avec Kubernetes__ : Déploiement automatisé et gestion des conteneurs via _Kubernetes_, assurant la résilience et la scalabilité.
   * __Infrastructure Cloud__ : Hébergée sur _AWS_ avec des machines virtuelles pour une fiabilité maximale et une flexibilité à grande échelle. 

Accès à l'application :
   * L'application est accessible à l'adresse : *todo (URL à définir)*.
