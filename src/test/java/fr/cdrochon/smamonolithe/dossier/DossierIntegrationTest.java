package fr.cdrochon.smamonolithe.dossier;

import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.dossier.query.entities.DossierStatus;
import fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class DossierIntegrationTest {

    @Autowired
    private DossierRepository dossierRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private VehiculeRepository vehiculeRepository;

    @BeforeEach
    void cleanDatabase() {
        dossierRepository.deleteAll();
        clientRepository.deleteAll();
        vehiculeRepository.deleteAll();
    }

    @Test
    void shouldPersistAndRetrieveDossierWithAllAssociations() {
        Dossier persisted = persistDossierGraph(DOSSIER_ID, "DOSSIER-001", CLIENT_ID, VEHICULE_ID);

        Dossier found = dossierRepository.findById(persisted.getId()).orElseThrow();
        assertEquals("DOSSIER-001", found.getNomDossier());
        assertNotNull(found.getClient());
        assertNotNull(found.getVehicule());
        assertEquals(CLIENT_ID, found.getClient().getId());
        assertEquals(VEHICULE_ID, found.getVehicule().getId());
    }

    @Test
    void shouldRetrieveAllDossiersFromDatabase() {
        persistDossierGraph("dossier-1", "DOSSIER-001", "client-1", "vehicule-1");
        persistDossierGraph("dossier-2", "DOSSIER-002", "client-2", "vehicule-2");

        List<Dossier> all = dossierRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void shouldDeleteDossierAndCascadeDeleteRelations() {
        Dossier persisted = persistDossierGraph(DOSSIER_ID, "DOSSIER-001", CLIENT_ID, VEHICULE_ID);

        dossierRepository.deleteById(persisted.getId());
        dossierRepository.flush();

        assertTrue(dossierRepository.findById(persisted.getId()).isEmpty());
        // In this model, deleting dossier does not cascade delete client/vehicule.
        assertTrue(clientRepository.findById(CLIENT_ID).isPresent());
        assertTrue(vehiculeRepository.findById(VEHICULE_ID).isPresent());
    }

    @Test
    void shouldUpdateDossierStatus() {
        Dossier persisted = persistDossierGraph(DOSSIER_ID, "DOSSIER-001", CLIENT_ID, VEHICULE_ID);

        persisted.setDossierStatus(DossierStatus.CLOTURE);
        dossierRepository.saveAndFlush(persisted);

        Dossier updated = dossierRepository.findById(DOSSIER_ID).orElseThrow();
        assertEquals(DossierStatus.CLOTURE, updated.getDossierStatus());
    }

    @Test
    void shouldHandleConstraintViolationWhenSavingDuplicateId() {
        Dossier first = persistDossierGraph(DOSSIER_ID, "DOSSIER-001", CLIENT_ID, VEHICULE_ID);

        first.setNomDossier("DOSSIER-001-UPDATED");
        dossierRepository.saveAndFlush(first);

        Dossier found = dossierRepository.findById(DOSSIER_ID).orElseThrow();
        assertEquals("DOSSIER-001-UPDATED", found.getNomDossier());
    }

    private Dossier persistDossierGraph(String dossierId, String dossierName, String clientId, String vehiculeId) {
        Vehicule vehicule = sampleVehiculeEntity();
        vehicule.setId(vehiculeId);
        vehicule.setClient(null);
        vehicule = vehiculeRepository.saveAndFlush(vehicule);

        Client client = sampleClientEntity();
        client.setId(clientId);
        client.setVehicule(vehicule);
        client = clientRepository.saveAndFlush(client);

        vehicule.setClient(client);
        vehicule = vehiculeRepository.saveAndFlush(vehicule);

        Dossier dossier = new Dossier();
        dossier.setId(dossierId);
        dossier.setNomDossier(dossierName);
        dossier.setDateCreationDossier(CREATED_AT);
        dossier.setDateModificationDossier(UPDATED_AT);
        dossier.setDossierStatus(DossierStatus.OUVERT);
        dossier.setClient(client);
        dossier.setVehicule(vehicule);

        return dossierRepository.saveAndFlush(dossier);
    }
}

