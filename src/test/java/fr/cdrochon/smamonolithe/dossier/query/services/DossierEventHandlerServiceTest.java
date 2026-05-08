package fr.cdrochon.smamonolithe.dossier.query.services;

import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import fr.cdrochon.smamonolithe.dossier.events.DossierCreatedEvent;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetDossierDTO;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
import org.hibernate.TransactionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DossierEventHandlerServiceTest {

    @Mock
    private DossierRepository dossierRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private VehiculeRepository vehiculeRepository;

    @Test
    void shouldPersistVehiculeClientAndDossierWhenEventIsReceived() {
        DossierEventHandlerService service = new DossierEventHandlerService(dossierRepository, clientRepository, vehiculeRepository);
        DossierCreatedEvent event = sampleDossierCreatedEvent();

        when(vehiculeRepository.save(any(Vehicule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dossierRepository.save(any(Dossier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.on(event);

        ArgumentCaptor<Vehicule> vehiculeCaptor = ArgumentCaptor.forClass(Vehicule.class);
        verify(vehiculeRepository, times(2)).save(vehiculeCaptor.capture());
        Vehicule firstVehiculeSave = vehiculeCaptor.getAllValues().get(0);
        Vehicule secondVehiculeSave = vehiculeCaptor.getAllValues().get(1);

        assertNotNull(firstVehiculeSave.getId());
        assertEquals(event.getVehicule().getImmatriculationVehicule(), firstVehiculeSave.getImmatriculationVehicule());
        assertNotNull(secondVehiculeSave.getClient());

        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(clientCaptor.capture());
        Client savedClient = clientCaptor.getValue();
        assertNotNull(savedClient.getId());
        assertEquals(event.getClient().getNomClient(), savedClient.getNomClient());
        assertNotNull(savedClient.getVehicule());

        ArgumentCaptor<Dossier> dossierCaptor = ArgumentCaptor.forClass(Dossier.class);
        verify(dossierRepository).save(dossierCaptor.capture());
        Dossier savedDossier = dossierCaptor.getValue();
        assertEquals(event.getId(), savedDossier.getId());
        assertEquals(event.getNomDossier(), savedDossier.getNomDossier());
        assertEquals(event.getDossierStatus(), savedDossier.getDossierStatus());
        assertNotNull(savedDossier.getClient());
        assertNotNull(savedDossier.getVehicule());
    }

    @Test
    void shouldWrapPersistenceErrorsInTransactionException() {
        DossierEventHandlerService service = new DossierEventHandlerService(dossierRepository, clientRepository, vehiculeRepository);
        DossierCreatedEvent event = sampleDossierCreatedEvent();
        when(vehiculeRepository.save(any(Vehicule.class))).thenThrow(new RuntimeException("db down"));

        TransactionException ex = assertThrows(TransactionException.class, () -> service.on(event));

        assertTrue(ex.getMessage().contains("Erreur lors de la creation du dossier"));
    }

    @Test
    void shouldReturnDossierByIdForQueryHandler() {
        DossierEventHandlerService service = new DossierEventHandlerService(dossierRepository, clientRepository, vehiculeRepository);
        when(dossierRepository.findById(DOSSIER_ID)).thenReturn(Optional.of(sampleDossierEntity()));

        DossierQueryDTO dto = service.on(new GetDossierDTO(DOSSIER_ID));

        assertNotNull(dto);
        assertEquals(DOSSIER_ID, dto.getId());
    }

    @Test
    void shouldThrowWhenDossierNotFoundForQueryHandler() {
        DossierEventHandlerService service = new DossierEventHandlerService(dossierRepository, clientRepository, vehiculeRepository);
        when(dossierRepository.findById(DOSSIER_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.on(new GetDossierDTO(DOSSIER_ID)));
    }

    @Test
    void shouldReturnAllDossiersForListQueryHandler() {
        DossierEventHandlerService service = new DossierEventHandlerService(dossierRepository, clientRepository, vehiculeRepository);
        when(dossierRepository.findAll()).thenReturn(List.of(sampleDossierEntity(), sampleDossierEntity()));

        List<DossierQueryDTO> result = service.on();

        assertEquals(2, result.size());
        assertEquals("DOSSIER-001", result.get(0).getNomDossier());
    }
}

