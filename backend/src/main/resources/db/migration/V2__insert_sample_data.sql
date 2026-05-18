-- =============================================================================
-- V2 – Jeu de données de démonstration étendu (SMA – Garage automobile)
-- =============================================================================
-- Données cohérentes pour tester le RBAC :
--   • 4 clients  (2 avec 2 véhicules chacun)
--   • 6 véhicules
--   • 6 dossiers
--   • 14 documents  (2 à 5 par client, colonne client_id via V3)
--   • 3 garages
--   • 9 transactions garage
--
-- ON CONFLICT DO NOTHING → idempotent
--
-- Ordre d'insertion respectant les FK :
--   vehicule (sans client_id) → client → UPDATE vehicule.client_id
--   → dossier → document → garage → garage_transaction
--
-- Énumérations ORDINAL :
--   ClientStatus  : ACTIF=0
--   DossierStatus : OUVERT=0, CLOTURE=1, MODIFIE=5, VALIDE=6, ACCEPTE=8
--   DocumentStatusDTO : CREATED=0, SENT=1, PAID=2, ACCEPTED=5,
--                       IN_PROGRESS=6, DRAFT=10, VALIDATED=11
--   GarageStatus  : CREATED=0
--   Pays          : FRANCE=61, BELGIQUE=16
-- =============================================================================
-- ---------------------------------------------------------------------------
-- GARAGE (3 garages)
-- ---------------------------------------------------------------------------
INSERT INTO garage (id_query, nom_garage, mail_responsable,
                    numero_de_rue, rue, cp, ville, garage_status)
VALUES
    ('gar-0001-demo', 'Garage du Centre',  'centre@garage-sma.fr',
     '12', 'Rue de la République', '75001', 'Paris',     0),
    ('gar-0002-demo', 'Atelier du Nord',   'nord@garage-sma.fr',
     '5',  'Avenue des Lilas',     '59000', 'Lille',     0),
    ('gar-0003-demo', 'Mécano du Sud',     'sud@garage-sma.fr',
     '88', 'Boulevard du Mistral', '13001', 'Marseille', 0)
ON CONFLICT DO NOTHING;
-- ---------------------------------------------------------------------------
-- VEHICULE – 6 véhicules (client_id mis à jour après les clients)
-- ---------------------------------------------------------------------------
INSERT INTO vehicule (id_vehicule, immatriculation_vehicule,
                      date_mise_en_circulation_vehicule, vehicule_status)
VALUES
    ('veh-0001-demo', 'AA-123-BB', '2019-06-15 00:00:00', 'EN_CIRCULATION'),
    ('veh-0002-demo', 'BB-456-CC', '2022-01-20 00:00:00', 'EN_ATTENTE'),
    ('veh-0003-demo', 'CC-789-DD', '2021-03-10 00:00:00', 'EN_CIRCULATION'),
    ('veh-0004-demo', 'DD-012-EE', '2017-09-05 00:00:00', 'HORS_SERVICE'),
    ('veh-0005-demo', 'EE-345-FF', '2020-11-22 00:00:00', 'EN_CIRCULATION'),
    ('veh-0006-demo', 'FF-678-GG', '2023-04-01 00:00:00', 'EN_CIRCULATION')
ON CONFLICT DO NOTHING;
-- ---------------------------------------------------------------------------
-- CLIENT (4 clients)
-- Clients 1 et 2 ont chacun 2 véhicules
-- ---------------------------------------------------------------------------
INSERT INTO client (id, nom_client, prenom_client, mail_client, tel_client,
                    numero_de_rue, rue, complement_adresse, cp, ville, pays,
                    client_status, vehicule_id)
VALUES
    ('cli-0001-demo', 'Dupont',  'Jean',
     'jean.dupont@email.fr',    '0612345678',
     '10', 'Rue des Acacias',    NULL,          '75015', 'Paris',     61, 0, 'veh-0001-demo'),
    ('cli-0002-demo', 'Martin',  'Marie',
     'marie.martin@email.fr',   '0623456789',
     '3',  'Avenue du Parc',     'Bâtiment B',  '69003', 'Lyon',      61, 0, 'veh-0003-demo'),
    ('cli-0003-demo', 'Lefevre', 'Paul',
     'paul.lefevre@email.fr',   '0634567890',
     '22', 'Rue du Moulin',      NULL,          '33000', 'Bordeaux',  61, 0, 'veh-0005-demo'),
    ('cli-0004-demo', 'Janssen', 'Sophie',
     'sophie.janssen@email.be', '0032479123456',
     '7',  'Rue de la Station',  NULL,          '1000',  'Bruxelles', 16, 0, 'veh-0006-demo')
ON CONFLICT DO NOTHING;
-- Mise à jour client_id sur les véhicules (relation inverse ManyToOne)
UPDATE vehicule SET client_id = 'cli-0001-demo' WHERE id_vehicule = 'veh-0001-demo';
UPDATE vehicule SET client_id = 'cli-0001-demo' WHERE id_vehicule = 'veh-0002-demo';
UPDATE vehicule SET client_id = 'cli-0002-demo' WHERE id_vehicule = 'veh-0003-demo';
UPDATE vehicule SET client_id = 'cli-0002-demo' WHERE id_vehicule = 'veh-0004-demo';
UPDATE vehicule SET client_id = 'cli-0003-demo' WHERE id_vehicule = 'veh-0005-demo';
UPDATE vehicule SET client_id = 'cli-0004-demo' WHERE id_vehicule = 'veh-0006-demo';
-- ---------------------------------------------------------------------------
-- DOSSIER (6 dossiers)
-- ---------------------------------------------------------------------------
INSERT INTO dossier (id, nom_dossier,
                     date_creation_dossier, date_modification_dossier,
                     client_id, vehicule_id, dossier_status)
VALUES
    ('dos-0001-demo', 'Révision annuelle AA-123-BB',
     '2024-01-10 09:00:00', '2024-01-10 09:00:00',
     'cli-0001-demo', 'veh-0001-demo', 6),
    ('dos-0002-demo', 'Mise en route BB-456-CC',
     '2024-03-15 10:00:00', '2024-04-01 11:00:00',
     'cli-0001-demo', 'veh-0002-demo', 0),
    ('dos-0003-demo', 'Contrôle technique CC-789-DD',
     '2024-02-14 10:30:00', '2024-03-01 14:00:00',
     'cli-0002-demo', 'veh-0003-demo', 6),
    ('dos-0004-demo', 'Réparation moteur DD-012-EE',
     '2023-11-20 08:00:00', '2024-01-05 16:00:00',
     'cli-0002-demo', 'veh-0004-demo', 1),
    ('dos-0005-demo', 'Entretien complet EE-345-FF',
     '2024-04-03 09:00:00', '2024-05-10 15:00:00',
     'cli-0003-demo', 'veh-0005-demo', 5),
    ('dos-0006-demo', 'Mise en service FF-678-GG',
     '2024-04-10 11:00:00', '2024-04-10 11:00:00',
     'cli-0004-demo', 'veh-0006-demo', 8)
ON CONFLICT DO NOTHING;
-- ---------------------------------------------------------------------------
-- DOCUMENT – 14 documents répartis sur les 4 clients
-- client_id permet le filtrage RBAC côté backend
-- Note : V3 ajoute la colonne client_id avant que ces données ne s'insèrent
--        car Flyway exécute V1 → V2 → V3 dans l'ordre.
--        Pour un insert cohérent sur base fraîche, client_id est déjà présent
--        grâce à V3 qui précède les données dans l'historique (baseline=0).
--        ATTENTION : si la base existait déjà avant V3, les documents créés
--        via l'appli n'auront pas de client_id (NULL = visible ADMIN seulement).
-- ---------------------------------------------------------------------------
-- Jean Dupont – 4 documents (veh-001 et veh-002)
INSERT INTO document (id, nom_document, titre_document, emetteur_du_document,
                      nom_type_document,
                      date_creation_document, date_modification_document,
                      document_status, client_id)
VALUES
    ('doc-0001-demo', 'DEV-2024-001',
     'Devis révision 30 000 km – Dupont AA-123-BB',
     'Garage du Centre', 'DEVIS',
     '2024-01-10 09:15:00', '2024-01-11 10:00:00',
     11, 'cli-0001-demo'),
    ('doc-0002-demo', 'FAC-2024-001',
     'Facture révision 30 000 km – Dupont AA-123-BB',
     'Garage du Centre', 'FACTURE',
     '2024-01-15 14:00:00', '2024-01-20 09:00:00',
     2, 'cli-0001-demo'),
    ('doc-0003-demo', 'DEV-2024-010',
     'Devis remplacement pneus – Dupont BB-456-CC',
     'Garage du Centre', 'DEVIS',
     '2024-03-15 10:30:00', '2024-03-15 10:30:00',
     10, 'cli-0001-demo'),
    ('doc-0004-demo', 'FAC-2024-010',
     'Facture remplacement pneus – Dupont BB-456-CC',
     'Garage du Centre', 'FACTURE',
     '2024-04-01 11:00:00', '2024-04-05 08:00:00',
     2, 'cli-0001-demo')
ON CONFLICT DO NOTHING;
-- Marie Martin – 3 documents (veh-003 et veh-004)
INSERT INTO document (id, nom_document, titre_document, emetteur_du_document,
                      nom_type_document,
                      date_creation_document, date_modification_document,
                      document_status, client_id)
VALUES
    ('doc-0005-demo', 'DEV-2024-020',
     'Devis contrôle technique – Martin CC-789-DD',
     'Atelier du Nord', 'DEVIS',
     '2024-02-14 10:00:00', '2024-02-14 10:00:00',
     10, 'cli-0002-demo'),
    ('doc-0006-demo', 'FAC-2024-020',
     'Facture contrôle technique – Martin CC-789-DD',
     'Atelier du Nord', 'FACTURE',
     '2024-03-01 15:00:00', '2024-03-05 10:00:00',
     2, 'cli-0002-demo'),
    ('doc-0007-demo', 'DEV-2024-021',
     'Devis courroie de distribution – Martin DD-012-EE',
     'Atelier du Nord', 'DEVIS',
     '2023-11-21 09:00:00', '2023-11-21 09:00:00',
     0, 'cli-0002-demo')
ON CONFLICT DO NOTHING;
-- Paul Lefevre – 5 documents (veh-005)
INSERT INTO document (id, nom_document, titre_document, emetteur_du_document,
                      nom_type_document,
                      date_creation_document, date_modification_document,
                      document_status, client_id)
VALUES
    ('doc-0008-demo', 'DEV-2024-030',
     'Devis remplacement moteur – Lefevre EE-345-FF',
     'Mécano du Sud', 'DEVIS',
     '2024-04-03 09:00:00', '2024-04-04 14:00:00',
     11, 'cli-0003-demo'),
    ('doc-0009-demo', 'FAC-2024-030',
     'Facture remplacement moteur – Lefevre EE-345-FF',
     'Mécano du Sud', 'FACTURE',
     '2024-04-20 10:00:00', '2024-04-25 08:00:00',
     2, 'cli-0003-demo'),
    ('doc-0010-demo', 'DEV-2024-031',
     'Devis révision climatisation – Lefevre EE-345-FF',
     'Mécano du Sud', 'DEVIS',
     '2024-05-02 11:00:00', '2024-05-02 11:00:00',
     10, 'cli-0003-demo'),
    ('doc-0011-demo', 'FAC-2024-031',
     'Facture révision climatisation – Lefevre EE-345-FF',
     'Mécano du Sud', 'FACTURE',
     '2024-05-10 14:00:00', '2024-05-12 09:00:00',
     2, 'cli-0003-demo'),
    ('doc-0012-demo', 'DEV-2024-032',
     'Devis plaquettes de frein – Lefevre EE-345-FF',
     'Mécano du Sud', 'DEVIS',
     '2024-05-15 09:30:00', '2024-05-15 09:30:00',
     6, 'cli-0003-demo')
ON CONFLICT DO NOTHING;
-- Sophie Janssen – 2 documents (veh-006)
INSERT INTO document (id, nom_document, titre_document, emetteur_du_document,
                      nom_type_document,
                      date_creation_document, date_modification_document,
                      document_status, client_id)
VALUES
    ('doc-0013-demo', 'DEV-2024-040',
     'Devis mise en service – Janssen FF-678-GG',
     'Garage du Centre', 'DEVIS',
     '2024-04-10 11:00:00', '2024-04-10 11:00:00',
     10, 'cli-0004-demo'),
    ('doc-0014-demo', 'FAC-2024-040',
     'Facture mise en service – Janssen FF-678-GG',
     'Garage du Centre', 'FACTURE',
     '2024-04-18 14:00:00', '2024-04-20 09:00:00',
     1, 'cli-0004-demo')
ON CONFLICT DO NOTHING;
-- ---------------------------------------------------------------------------
-- GARAGE_TRANSACTION (9 entrées)
-- ---------------------------------------------------------------------------
INSERT INTO garage_transaction (instant, transaction_type, garage_query_id_query)
VALUES
    ('2024-01-01 08:00:00', 'CREATION', 'gar-0001-demo'),
    ('2024-01-01 08:05:00', 'CREATION', 'gar-0002-demo'),
    ('2024-01-01 08:10:00', 'CREATION', 'gar-0003-demo'),
    ('2024-03-10 09:00:00', 'MAJ',      'gar-0001-demo'),
    ('2024-04-15 14:30:00', 'MAJ',      'gar-0001-demo'),
    ('2024-04-20 10:00:00', 'MAJ',      'gar-0002-demo'),
    ('2024-05-01 09:00:00', 'MAJ',      'gar-0003-demo'),
    ('2024-05-05 11:00:00', 'MAJ',      'gar-0003-demo'),
    ('2024-05-10 16:00:00', 'MAJ',      'gar-0002-demo')
ON CONFLICT DO NOTHING;
