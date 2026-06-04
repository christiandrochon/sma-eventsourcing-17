<!--
═════════════════════════════════════════════════════════════════════════════
LOGGING_STRATEGY.md
═════════════════════════════════════════════════════════════════════════════
Qu'il contient : Guide stratégique complet du logging (métier + technique)
Utilité : Architecture de logs, conventions de nommage, fichiers générés, configuration
Public : Développeurs, architects (documentation de design)
À consulter : Avant d'ajouter des logs, pour suivre la stratégie
À archiver : Non (spécification technique en vigueur)
═════════════════════════════════════════════════════════════════════════════
-->

# Stratégie de Logging Métier - SMA Event Sourcing

## Vue d'ensemble

Ce document décrit la stratégie de logging métier implémentée dans l'application SMA Event Sourcing. Le logging est organisé en deux niveaux : **technique** et **métier**.

---

## 1. BACKEND (Port 8092)

### Configuration
- **File de configuration** : `backend/src/main/resources/logback-spring.xml`
- **Logger métier** : `BUSINESS`

### Logs Métier (business.log)

Les opérations métier critiques sont loggées via `BusinessLoggers.business()` :

#### Création d'Entités

| Entité | Format de Log | Location |
|--------|--------------|----------|
| **Client** | `BIZ_CLIENT_CREATE_REQUEST clientId={} nomClient={}` | `ClientCommandService.createClient()` |
| | `BIZ_CLIENT_CREATE_CONFIRMED clientId={} status={}` | `ClientCommandService.completeClientCreation()` |
| | `BIZ_CLIENT_CREATED clientId={} nomClient={} prenomClient={} status={}` | `ClientEventHandler.on()` |
| **Dossier** | `BIZ_DOSSIER_CREATE_REQUEST dossierId={} nomDossier={} clientId={} vehiculeId={} status={}` | `DossierCommandService.createDossier()` |
| | `BIZ_DOSSIER_CREATE_CONFIRMED dossierId={} clientId={} vehiculeId={} status={}` | `DossierCommandService.completeDossierCreation()` |
| | `BIZ_DOSSIER_CREATED dossierId={} nomDossier={} clientId={} vehiculeId={} status={}` | `DossierEventHandler.on()` |
| **Véhicule** | `BIZ_VEHICULE_CREATE_REQUEST vehiculeId={} immatriculation={} status={}` | `VehiculeCommandService.createVehicule()` |
| | `BIZ_VEHICULE_CREATE_CONFIRMED vehiculeId={} immatriculation={} status={}` | `VehiculeCommandService.completeVehiculeCreation()` |
| | `BIZ_VEHICULE_CREATED vehiculeId={} immatriculation={} status={}` | `VehiculeEventHandler.on()` |
| **Document** | `BIZ_DOCUMENT_CREATE_REQUEST documentId={} nomDocument={} type={} status={}` | `DocumentCommandService.createDocument()` |
| | `BIZ_DOCUMENT_CREATE_CONFIRMED documentId={} nomDocument={} status={}` | `DocumentCommandService.completeDocumentCreation()` |
| | `BIZ_DOCUMENT_CREATED documentId={} nomDocument={} type={} status={}` | `DocumentEventHandler.on()` |
| **Garage** | `BIZ_GARAGE_CREATE_REQUEST garageId={} nomGarage={}` | `GarageCommandService.createGarage()` |
| | `BIZ_GARAGE_CREATE_CONFIRMED garageId={} nomGarage={}` | `GarageCommandService.completeGarageCreation()` |
| | `BIZ_GARAGE_CREATED garageId={} nomGarage={} mailResponsable={}` | `GarageEventHandler.on()` |

### Flux de Logging Métier
```
1. Request reçue (CommandService)
   → BIZ_*_CREATE_REQUEST

2. Événement déclenché (EventHandler)
   → BIZ_*_CREATED

3. Création complétée (CommandService.complete)
   → BIZ_*_CREATE_CONFIRMED
```

### Fichiers de Sortie
- **business.log** : Tous les événements métier
- **technical.log** : Erreurs techniques (WARN et supérieur)

---

## 2. FRONTEND (Port 8091)

### Configuration
- **File de configuration** : `frontend/src/main/resources/logback-spring.xml`
- **Loggers** : `UI_ACCESS`, `UI_ERROR`, `UI_TECH`, `UI_BUSINESS`

### Logs Métier (ui-business.log)

Les opérations métier côté frontend sont loggées via `FrontendLoggers.business()` :

#### Requêtes de Création

| Opération | Format de Log | Location |
|-----------|--------------|----------|
| **Client - Création** | `UI_CLIENT_CREATE_REQUEST nomClient={} prenomClient={} mail={} tel={} status={}` | `CreateClientThymController.createClient()` |
| | `UI_CLIENT_CREATE_OK clientId={}` | `CreateClientThymController` (succès) |
| **Dossier - Création** | `UI_DOSSIER_CREATE_REQUEST nomDossier={} clientNom={} vehiculeImmatriculation={} status={}` | `CreateDossierThymController.createDossierAsync()` |
| **Véhicule - Création** | `UI_VEHICULE_CREATE_REQUEST immatriculation={} dateMiseEnCirculation={} status={}` | `CreateVehiculeThymController.createDossierAsync()` |
| **Document - Création** | `UI_DOCUMENT_CREATE_REQUEST nomDocument={} titre={} emetteur={} type={} status={}` | `CreateDocumentThymController.createDocumentAsync()` |

#### Requêtes de Consultation

| Opération | Format de Log | Location |
|-----------|--------------|----------|
| **Client - Détail** | `UI_CLIENT_QUERY clientId={}` | `ClientThymController.getClientByIdAsync()` |
| | `UI_CLIENT_RETRIEVED clientId={} nomClient={}` | Après récupération réussie |
| **Clients - Liste** | `UI_CLIENTS_LIST_REQUEST` | `ClientThymController.getClientsAsync()` |
| | `UI_CLIENTS_LIST_RETRIEVED count={}` | Après récupération réussie |

### Erreurs Métier

Les erreurs d'opérations métier sont loggées via `FrontendLoggers.error()` :
- Validation : `UI_*_VALIDATION_ERROR field={}`
- Timeout : `UI_*_FAILED reason=timeout`
- Erreurs HTTP : `UI_*_FAILED status={} message={}`

### Fichiers de Sortie
- **ui-access.log** : Actions utilisateur et succès
- **ui-error.log** : Erreurs (validation, timeout, HTTP)
- **ui-business.log** : Événements métier critiques
- **ui-technical.log** : Traces techniques

---

## 3. Nommage des Logs Métier

### Convention de Nommage
```
[UI_][MODULE]_[ACTION]_[STATE]
```

**Exemple** :
- `BIZ_CLIENT_CREATE_REQUEST` - Demande de création d'un client
- `UI_DOSSIER_CREATE_REQUEST` - Demande frontend de création d'un dossier
- `BIZ_VEHICULE_CREATED` - Événement : véhicule créé
- `UI_CLIENT_QUERY` - Consultation d'un client

### Modules Principaux
- **CLIENT** - Opérations sur les clients
- **DOSSIER** - Opérations sur les dossiers
- **VEHICULE** - Opérations sur les véhicules
- **DOCUMENT** - Opérations sur les documents
- **GARAGE** - Opérations sur les garages

### Actions
- **CREATE** - Création d'une entité
- **QUERY** - Consultation/Requête de données
- **LIST** - Récupération d'une liste
- **RETRIEVED** - Données récupérées avec succès
- **CREATED** - Événement de création déclenché
- **CONFIRMED** - Création confirmée

### États
- **REQUEST** - Demande initiale
- **CONFIRMED** - Confirmation/Acceptation
- **FAILED** - Échec
- **VALIDATION_ERROR** - Erreur de validation

---

## 4. Flux d'Événements Complet

### Création d'une Entité (Exemple : Client)

```
Frontend (Port 8091)
  ↓
  UI_CLIENT_CREATE_REQUEST
  ↓
  WebClient.post() → Backend
  ↓
Backend (Port 8092)
  ↓
  BIZ_CLIENT_CREATE_REQUEST
  ↓
  ClientCommandService.createClient()
  ↓
  Axon CommandGateway
  ↓
  ClientEventHandler.on(ClientCreatedEvent)
  ↓
  BIZ_CLIENT_CREATED
  ↓
  ClientCommandService.completeClientCreation()
  ↓
  BIZ_CLIENT_CREATE_CONFIRMED
  ↓
  Frontend (WebClient reçoit la réponse)
  ↓
  UI_CLIENT_CREATE_OK
```

---

## 5. Accès aux Logs

### Backend

**Logs métier** :
```bash
tail -f backend/logs/business.log
```

**Erreurs techniques** :
```bash
tail -f backend/logs/technical.log
```

### Frontend

**Logs métier** :
```bash
tail -f frontend/logs/ui-business.log
```

**Actions utilisateur** :
```bash
tail -f frontend/logs/ui-access.log
```

**Erreurs** :
```bash
tail -f frontend/logs/ui-error.log
```

**Traces techniques** :
```bash
tail -f frontend/logs/ui-technical.log
```

---

## 6. Utilitaires de Logging

### Backend
```java
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;

// Utilisation
BusinessLoggers.business().info("BIZ_CLIENT_CREATE_REQUEST clientId={}", clientId);
```

### Frontend
```java
import fr.cdrochon.thymeleaffrontend.logging.FrontendLoggers;

// Logs métier
FrontendLoggers.business().info("UI_CLIENT_CREATE_REQUEST clientId={}", clientId);

// Actions utilisateur
FrontendLoggers.access().info("UI_CLIENT_CREATE_OK clientId={}", clientId);

// Erreurs
FrontendLoggers.error().error("UI_CLIENT_CREATE_FAILED status={}", status);

// Traces techniques
FrontendLoggers.tech().info("Response time: {}ms", duration);
```

---

## 7. Rotation des Fichiers de Log

**Configuration appliquée** :
- **Taille max** : 10 MB par fichier
- **Historique** : 14 jours
- **Capacité totale** : 1 GB (backend), 500 MB (frontend)
- **Format d'archive** : `log.YYYY-MM-DD.i.log.gz`

---

## 8. Améliorations Futures

1. **Contexte utilisateur** : Ajouter l'ID utilisateur/session dans les logs
2. **Traçabilité distribuée** : Implémenter un correlation ID entre frontend et backend
3. **Alertes** : Configurer des alertes pour les erreurs métier critiques
4. **Analyse** : Intégrer ELK Stack pour l'analyse des logs
5. **Performance** : Logger les métriques de performance (latence, temps d'exécution)

---

## 9. Checklist de Validation

- [x] Logger métier implémenté au backend
- [x] Logger métier implémenté au frontend
- [x] Configuration logback complète
- [x] Nommage cohérent des logs
- [x] Séparation technique/métier
- [x] Rotation des fichiers activée
- [ ] Corrélation ID distribuée (futur)
- [ ] ELK Stack intégré (futur)



