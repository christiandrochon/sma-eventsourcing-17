package fr.cdrochon.smamonolithe.client;

import fr.cdrochon.smamonolithe.client.events.ClientCreatedEvent;
import fr.cdrochon.smamonolithe.client.query.dtos.GetClientDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import fr.cdrochon.smamonolithe.client.query.services.ClientEventHandlerService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static fr.cdrochon.smamonolithe.client.ClientTestDataFactory.sampleClientCreatedEvent;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Active l'extension Mockito de JUnit 5 pour initialiser et injecter automatiquement
 * les mocks utilises par ce test.
 */
@ExtendWith(MockitoExtension.class)
class ClientEventHandlerServiceErrorCasesTest {

    @Mock
    private ClientRepository clientRepository;

    @Test
    void shouldNotPropagateExceptionWhenRepositorySaveThrows() {
        /**
         * Le catch dans le service avale l'exception → ne doit pas throw vers l'appelant
         */
        when(clientRepository.save(any(Client.class))).thenThrow(new RuntimeException("DB down"));
        ClientEventHandlerService service = new ClientEventHandlerService(clientRepository, org.mockito.Mockito.mock(ApplicationEventPublisher.class));
        ClientCreatedEvent event = sampleClientCreatedEvent();
        assertDoesNotThrow(() -> service.on(event));
    }

    @Test
    void shouldThrowEntityNotFoundForUnknownId() {
        when(clientRepository.findById("ghost-id")).thenReturn(Optional.empty());
        ClientEventHandlerService service = new ClientEventHandlerService(clientRepository, org.mockito.Mockito.mock(ApplicationEventPublisher.class));
        GetClientDTO query = new GetClientDTO("ghost-id");
        assertThrows(EntityNotFoundException.class, () -> service.on(query));
    }

    @Test
    void shouldThrowEntityNotFoundForNullId() {
        when(clientRepository.findById(null)).thenReturn(Optional.empty());
        ClientEventHandlerService service = new ClientEventHandlerService(clientRepository, org.mockito.Mockito.mock(ApplicationEventPublisher.class));
        GetClientDTO query = new GetClientDTO(null);
        assertThrows(EntityNotFoundException.class, () -> service.on(query));
    }

    @Test
    void shouldHandleConstraintViolationOnDuplicateSave() {
        when(clientRepository.save(any(Client.class)))
            .thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate entry"));
        ClientEventHandlerService service = new ClientEventHandlerService(clientRepository, org.mockito.Mockito.mock(ApplicationEventPublisher.class));
        ClientCreatedEvent event = sampleClientCreatedEvent();
        /**
         * Le catch interne avale → pas de throw
         */
        assertDoesNotThrow(() -> service.on(event));
    }

    @Test
    void shouldReturnEmptyListWhenFindAllReturnsEmpty() {
        when(clientRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        ClientEventHandlerService service = new ClientEventHandlerService(clientRepository, org.mockito.Mockito.mock(ApplicationEventPublisher.class));
        assertTrue(service.on().isEmpty());
    }
}
