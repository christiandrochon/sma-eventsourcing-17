package fr.cdrochon.smamonolithe.client;

import fr.cdrochon.smamonolithe.client.events.ClientCreatedEvent;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetClientDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.entities.ClientStatus;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import fr.cdrochon.smamonolithe.client.query.services.ClientEventHandlerService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static fr.cdrochon.smamonolithe.client.ClientTestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientEventHandlerServiceTest {

    @Mock
    private ClientRepository clientRepository;

    private ClientEventHandlerService service;

    @BeforeEach
    void setUp() {
        service = new ClientEventHandlerService(clientRepository, org.mockito.Mockito.mock(ApplicationEventPublisher.class));
    }

    @Test
    void shouldSaveClientOnCreatedEvent() {
        ClientCreatedEvent event = sampleClientCreatedEvent();
        service.on(event);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void shouldSaveClientWithCorrectNom() {
        ClientCreatedEvent event = sampleClientCreatedEvent();
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        service.on(event);
        verify(clientRepository).save(captor.capture());
        assertEquals("Dupont", captor.getValue().getNomClient());
    }

    @Test
    void shouldSaveClientWithCorrectStatus() {
        ClientCreatedEvent event = sampleClientCreatedEvent();
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        service.on(event);
        verify(clientRepository).save(captor.capture());
        assertEquals(ClientStatus.ACTIF, captor.getValue().getClientStatus());
    }

    @Test
    void shouldSaveClientWithCorrectAdresse() {
        ClientCreatedEvent event = sampleClientCreatedEvent();
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        service.on(event);
        verify(clientRepository).save(captor.capture());
        assertEquals("Paris", captor.getValue().getAdresse().getVille());
    }

    @Test
    void shouldReturnClientDTOWhenFound() {
        when(clientRepository.findById("client-uuid-1")).thenReturn(Optional.of(sampleClient()));
        ClientQueryDTO dto = service.on(new GetClientDTO("client-uuid-1"));
        assertNotNull(dto);
        assertEquals("client-uuid-1", dto.getId());
    }

    @Test
    void shouldThrowEntityNotFoundWhenClientNotFound() {
        when(clientRepository.findById("unknown")).thenReturn(Optional.empty());
        GetClientDTO query = new GetClientDTO("unknown");
        assertThrows(EntityNotFoundException.class, () -> service.on(query));
    }

    @Test
    void shouldReturnAllClients() {
        when(clientRepository.findAll()).thenReturn(List.of(sampleClient()));
        List<ClientQueryDTO> result = service.on();
        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoClients() {
        when(clientRepository.findAll()).thenReturn(Collections.emptyList());
        List<ClientQueryDTO> result = service.on();
        assertTrue(result.isEmpty());
    }
}
