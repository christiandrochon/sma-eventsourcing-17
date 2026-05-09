# Guide Rapide - Logging Métier SMA

## 🎯 Objectif
Tracer toutes les opérations métier critiques (création, lecture) pour audit et débogage.

---

## 📊 Afficher les Logs Métier

### Backend
```bash
# Tous les événements métier
tail -f backend/logs/business.log

# Suivre en temps réel
tail -f -v backend/logs/business.log

# Chercher une création de client
grep "BIZ_CLIENT_CREATE" backend/logs/business.log

# Chercher une création spécifique (client 123-abc)
grep "BIZ_CLIENT.*123-abc" backend/logs/business.log
```

### Frontend
```bash
# Tous les événements métier frontend
tail -f frontend/logs/ui-business.log

# Événements métier + accès utilisateur
tail -f frontend/logs/ui-*.log | grep -E "BUSINESS|ACCESS"

# Chercher une création de dossier
grep "UI_DOSSIER_CREATE" frontend/logs/ui-business.log

# Chercher une erreur
grep "UI_.*FAILED\|VALIDATION_ERROR" frontend/logs/ui-error.log
```

---

## 📝 Exemples de Logs

### ✅ Création Réussie d'un Client

**Frontend** (ui-business.log) :
```
2026-05-09 14:30:45.123 INFO  UI_CLIENT_CREATE_REQUEST nomClient=Dupont prenomClient=Jean mail=jean@example.com tel=0123456789 status=ACTIVE
```

**Backend** (business.log) :
```
2026-05-09 14:30:46.456 INFO  BIZ_CLIENT_CREATE_REQUEST clientId=abc-123-def nomClient=Dupont
2026-05-09 14:30:46.789 INFO  BIZ_CLIENT_CREATED clientId=abc-123-def nomClient=Dupont prenomClient=Jean status=ACTIVE
2026-05-09 14:30:47.012 INFO  BIZ_CLIENT_CREATE_CONFIRMED clientId=abc-123-def status=ACTIVE
```

**Frontend** (ui-access.log) :
```
2026-05-09 14:30:47.500 INFO  UI_CLIENT_CREATE_OK clientId=abc-123-def
```

### ❌ Création Échouée d'un Client (Validation)

**Frontend** (ui-error.log) :
```
2026-05-09 14:35:20.100 WARN  UI_CLIENT_CREATE_VALIDATION_ERROR field=Email invalide
```

### ⏱️ Timeout de Création d'un Document

**Frontend** (ui-error.log) :
```
2026-05-09 15:00:30.500 ERROR UI_DOCUMENT_CREATE_FAILED reason=timeout message=Request timeout after 3000ms
```

### 📊 Consultation de Données

**Frontend** (ui-business.log) :
```
2026-05-09 14:45:10.200 INFO  UI_CLIENT_QUERY clientId=abc-123-def
```

**Frontend** (ui-access.log) :
```
2026-05-09 14:45:10.567 INFO  UI_CLIENT_RETRIEVED clientId=abc-123-def nomClient=Dupont
```

---

## 🔍 Recherches Utiles

### Chercher toutes les opérations d'un client
```bash
# Backend
grep "abc-123-def" backend/logs/business.log

# Frontend
grep "abc-123-def" frontend/logs/ui-*.log
```

### Lister toutes les créations d'un type
```bash
# Clients
grep "BIZ_CLIENT_CREATE\|UI_CLIENT_CREATE" backend/logs/business.log frontend/logs/ui-business.log

# Dossiers
grep "BIZ_DOSSIER_CREATE\|UI_DOSSIER_CREATE" backend/logs/business.log frontend/logs/ui-business.log
```

### Comptabiliser les opérations par jour
```bash
# Créations de clients
grep "BIZ_CLIENT_CREATE_REQUEST" backend/logs/business.log | wc -l

# Erreurs par type
grep "FAILED\|ERROR" frontend/logs/ui-error.log | cut -d' ' -f4 | sort | uniq -c
```

### Afficher les 10 dernières opérations
```bash
tail -10 backend/logs/business.log
tail -10 frontend/logs/ui-business.log
```

---

## 🚀 Utiliser les Logs dans le Code

### Backend
```java
// Import
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;

// Utilisation
BusinessLoggers.business().info("BIZ_CLIENT_CREATE_REQUEST clientId={} nomClient={}", 
                               clientId, nomClient);
```

### Frontend
```java
// Import
import fr.cdrochon.thymeleaffrontend.logging.FrontendLoggers;

// Événement métier
FrontendLoggers.business().info("UI_CLIENT_CREATE_REQUEST nomClient={}", nomClient);

// Action utilisateur
FrontendLoggers.access().info("UI_CLIENT_CREATE_OK clientId={}", clientId);

// Erreur
FrontendLoggers.error().error("UI_CLIENT_CREATE_FAILED reason={}", reason);

// Trace technique
FrontendLoggers.tech().debug("Response time: {}ms", duration);
```

---

## 📈 Monitorer les Performances

### Mesurer la latence
```bash
# Temps entre REQUEST et CONFIRMED (backend)
# Exemple : REQUEST à 14:30:46.456, CONFIRMED à 14:30:47.012 = 556ms

grep "BIZ_CLIENT_CREATE_REQUEST" backend/logs/business.log
grep "BIZ_CLIENT_CREATE_CONFIRMED" backend/logs/business.log
```

### Comptabiliser les erreurs
```bash
# Total erreurs par jour
grep "FAILED" frontend/logs/ui-error.log | wc -l

# Erreurs par statut HTTP
grep "UI_.*_FAILED status=" frontend/logs/ui-error.log | cut -d'=' -f2 | sort | uniq -c
```

---

## 🔧 Dépannage

### Logs non visibles?
```bash
# Vérifier que le fichier existe
ls -la backend/logs/
ls -la frontend/logs/

# Vérifier les permissions
chmod 755 backend/logs frontend/logs

# Vérifier que logback est activé
grep "logback-spring.xml" backend/target/classes/
grep "logback-spring.xml" frontend/target/classes/
```

### Logs trop verbeux?
Modifier le niveau dans `logback-spring.xml` :
```xml
<logger name="BUSINESS" level="WARN">  <!-- Changé de INFO à WARN -->
```

### Logs trop compressés?
Vérifier la rotation :
```bash
# Voir les fichiers compressés
ls -lah backend/logs/business.*.log.gz
```

---

## 📋 Checklist de Monitoring

- [ ] Vérifier que `business.log` se remplit (backend)
- [ ] Vérifier que `ui-business.log` se remplit (frontend)
- [ ] Chercher les patterns d'erreur (FAILED, VALIDATION)
- [ ] Monitorer la latence (REQUEST → CONFIRMED)
- [ ] Archiver les vieux logs (au-delà de 14 jours)

---

## 📞 Support

**Problème de logs?**
1. Vérifier les permissions des répertoires
2. Vérifier la configuration logback-spring.xml
3. Vérifier les imports (`BusinessLoggers`, `FrontendLoggers`)
4. Recompiler le projet : `mvn clean install`

---

**Documentation complète** : Voir `LOGGING_STRATEGY.md`

