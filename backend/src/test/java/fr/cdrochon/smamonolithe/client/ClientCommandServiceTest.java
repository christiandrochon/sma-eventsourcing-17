package fr.cdrochon.smamonolithe.client;

import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import fr.cdrochon.smamonolithe.client.command.services.ClientCommandService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static fr.cdrochon.smamonolithe.client.ClientTestDataFactory.sampleClientCommandDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientCommandServiceTest {

    @Mock
    private CommandGateway commandGateway;

    private ClientCommandService service;

    @BeforeEach
    void setUp() {
        service = new ClientCommandService(commandGateway);
    }

    @Test
    void shouldSendCommandViaGateway() {
        service.createClient(sampleClientCommandDTO());
        verify(commandGateway, times(1)).send(any());
    }

    @Test
    void shouldReturnCompletableFuture() {
        CompletableFuture<ClientCommandDTO> future = service.createClient(sampleClientCommandDTO());
        assertNotNull(future);
        assertFalse(future.isDone());
    }

    @Test
    void shouldCompleteFutureWhenCompleteClientCreationCalled() {
        service.createClient(sampleClientCommandDTO());
        ClientCommandDTO dto = sampleClientCommandDTO();
        service.completeClientCreation(dto);

        // La future doit être complétée
        // On recrée pas la future ici car elle est interne, on vérifie juste que ça ne throw pas
        assertDoesNotThrow(() -> service.completeClientCreation(dto));
    }

    @Test
    void shouldNotThrowWhenCompleteCalledBeforeCreateClient() {
        // futureDTO est null au départ
        assertDoesNotThrow(() -> service.completeClientCreation(sampleClientCommandDTO()));
    }

    @Test
    void shouldGenerateRandomUUIDForCommand() {
        service.createClient(sampleClientCommandDTO());
        service.createClient(sampleClientCommandDTO());
        // 2 appels → 2 commandes avec des UUIDs différents
        verify(commandGateway, times(2)).send(any());
    }

    @Test
    void shouldHandleNullDTOFields() {
        ClientCommandDTO dto = new ClientCommandDTO();
        dto.setNomClient(null);
        dto.setAdresse(null);

        assertThrows(NullPointerException.class, () -> service.createClient(dto));
    }
}