package fr.cdrochon.smamonolithe.dossier.query.services;

import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import fr.cdrochon.smamonolithe.dossier.events.DossierCreatedEvent;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetDossierDTO;
import fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.TransactionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.Optional;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.sampleDossierCreatedEvent;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DossierEventHandlerServiceErrorCasesTest {

    @Mock
    private DossierRepository dossierRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private VehiculeRepository vehiculeRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void shouldHandleEventWhenVehiculeRepositoryThrowsException() {
        DossierEventHandlerService service = new DossierEventHandlerService(dossierRepository, clientRepository, vehiculeRepository, applicationEventPublisher);
        DossierCreatedEvent event = sampleDossierCreatedEvent();
        when(vehiculeRepository.save(any(Vehicule.class))).thenThrow(new RuntimeException("Database connection lost"));

        TransactionException ex = assertThrows(TransactionException.class, () -> service.on(event));
        assertTrue(ex.getMessage().contains("Erreur lors de la creation du dossier"));
    }

    @Test
    void shouldThrowEntityNotFoundWhenQueryingNonExistentDossier() {
        DossierEventHandlerService service = new DossierEventHandlerService(dossierRepository, clientRepository, vehiculeRepository, applicationEventPublisher);
        when(dossierRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.on(new GetDossierDTO("non-existent-id")));
        assertEquals("Dossier non trouvé", ex.getMessage());
    }

    @Test
    void shouldReturnEmptyListWhenNoDossiersExist() {
        DossierEventHandlerService service = new DossierEventHandlerService(dossierRepository, clientRepository, vehiculeRepository, applicationEventPublisher);
        when(dossierRepository.findAll()).thenReturn(Collections.emptyList());

        var result = service.on();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

