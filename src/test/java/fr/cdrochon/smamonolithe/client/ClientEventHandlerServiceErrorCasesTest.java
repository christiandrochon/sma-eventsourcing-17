package fr.cdrochon.smamonolithe.client.query.services;

import fr.cdrochon.smamonolithe.client.events.ClientCreatedEvent;
import fr.cdrochon.smamonolithe.client.query.dtos.GetClientDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static fr.cdrochon.smamonolithe.client.ClientTestDataFactory.sampleClientCreatedEvent;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientEventHandlerServiceErrorCasesTest {

    @Mock
    private ClientRepository clientRepository;

    @Test
    void shouldNotPropagateExceptionWhenRepositorySaveThrows() {
        // Le catch dans le service avale l'exception → ne doit pas throw vers l'appelant
        when(clientRepository.save(any(Client.class))).thenThrow(new RuntimeException("DB down"));
        ClientEventHandlerService service = new ClientEventHandlerService(clientRepository);
        ClientCreatedEvent event = sampleClientCreatedEvent();
        assertDoesNotThrow(() -> service.on(event));
    }

    @Test
    void shouldThrowEntityNotFoundForUnknownId() {
        when(clientRepository.findById("ghost-id")).thenReturn(Optional.empty());
        ClientEventHandlerService service = new ClientEventHandlerService(clientRepository);
        assertThrows(EntityNotFoundException.class, () -> service.on(new GetClientDTO("ghost-id")));
    }

    @Test
    void shouldThrowEntityNotFoundForNullId() {
        when(clientRepository.findById(null)).thenReturn(Optional.empty());
        ClientEventHandlerService service = new ClientEventHandlerService(clientRepository);
        assertThrows(EntityNotFoundException.class, () -> service.on(new GetClientDTO(null)));
    }

    @Test
    void shouldHandleConstraintViolationOnDuplicateSave() {
        when(clientRepository.save(any(Client.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate entry"));
        ClientEventHandlerService service = new ClientEventHandlerService(clientRepository);
        // Le catch interne avale → pas de throw
        assertDoesNotThrow(() -> service.on(sampleClientCreatedEvent()));
    }

    @Test
    void shouldReturnEmptyListWhenFindAllReturnsEmpty() {
        when(clientRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        ClientEventHandlerService service = new ClientEventHandlerService(clientRepository);
        assertTrue(service.on().isEmpty());
    }
}