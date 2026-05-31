package fr.cdrochon.smamonolithe.client;

import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import fr.cdrochon.smamonolithe.client.command.services.ClientCommandService;
import fr.cdrochon.smamonolithe.client.events.ClientEventHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static fr.cdrochon.smamonolithe.client.ClientTestDataFactory.sampleClientCreatedEvent;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Active l'extension Mockito de JUnit 5 pour initialiser et injecter automatiquement
 * les mocks utilises par ce test.
 */
@ExtendWith(MockitoExtension.class)
class ClientEventHandlerTest {

    @Mock
    private ClientCommandService clientCommandService;

    @InjectMocks
    private ClientEventHandler clientEventHandler;

    @Test
    void shouldCallCompleteClientCreationOnEvent() {
        clientEventHandler.on(sampleClientCreatedEvent());
        verify(clientCommandService, times(1)).completeClientCreation(any());
    }

    @Test
    void shouldPassCorrectNomClientToService() {
        ArgumentCaptor<ClientCommandDTO> captor = ArgumentCaptor.forClass(ClientCommandDTO.class);
        clientEventHandler.on(sampleClientCreatedEvent());
        verify(clientCommandService).completeClientCreation(captor.capture());
        assertEquals("Dupont", captor.getValue().getNomClient());
    }

    @Test
    void shouldPassCorrectMailClientToService() {
        ArgumentCaptor<ClientCommandDTO> captor = ArgumentCaptor.forClass(ClientCommandDTO.class);
        clientEventHandler.on(sampleClientCreatedEvent());
        verify(clientCommandService).completeClientCreation(captor.capture());
        assertEquals("jean.dupont@mail.com", captor.getValue().getMailClient());
    }

    @Test
    void shouldPassCorrectIdToService() {
        ArgumentCaptor<ClientCommandDTO> captor = ArgumentCaptor.forClass(ClientCommandDTO.class);
        clientEventHandler.on(sampleClientCreatedEvent());
        verify(clientCommandService).completeClientCreation(captor.capture());
        assertEquals("client-uuid-1", captor.getValue().getId());
    }

    @Test
    void shouldConvertAdresseToDTOBeforeCallingService() {
        ArgumentCaptor<ClientCommandDTO> captor = ArgumentCaptor.forClass(ClientCommandDTO.class);
        clientEventHandler.on(sampleClientCreatedEvent());
        verify(clientCommandService).completeClientCreation(captor.capture());
        assertNotNull(captor.getValue().getAdresse());
        assertEquals("Paris", captor.getValue().getAdresse().getVille());
    }
}
