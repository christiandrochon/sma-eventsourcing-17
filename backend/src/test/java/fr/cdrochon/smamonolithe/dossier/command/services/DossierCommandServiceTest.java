package fr.cdrochon.smamonolithe.dossier.command.services;

import fr.cdrochon.smamonolithe.dossier.command.commands.DossierCreateCommand;
import fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static fr.cdrochon.smamonolithe.dossier.DossierTestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
/**
 * Cette classe contient uniquement des tests unitaires.
 */
class DossierCommandServiceTest {

    @Mock
    private CommandGateway commandGateway;

    private DossierCommandService service;

    @BeforeEach
    void setUp() {
        service = new DossierCommandService(commandGateway);
        org.mockito.Mockito.lenient().when(commandGateway.send(any(DossierCreateCommand.class)))
                .thenReturn(CompletableFuture.completedFuture("ok"));
    }

    @Test
    void shouldSendCreateCommandAndReturnFuture() {
        DossierCommandDTO dto = sampleDossierCommandDto();
        when(commandGateway.send(any(DossierCreateCommand.class))).thenReturn(CompletableFuture.completedFuture("ok"));

        CompletableFuture<DossierCommandDTO> future = service.createDossier(dto);

        assertNotNull(future);
        assertFalse(future.isDone());

        ArgumentCaptor<DossierCreateCommand> captor = ArgumentCaptor.forClass(DossierCreateCommand.class);
        verify(commandGateway).send(captor.capture());
        DossierCreateCommand sent = captor.getValue();

        assertNotNull(sent.getId());
        assertEquals(dto.getNomDossier(), sent.getNomDossier());
        assertEquals(dto.getDateCreationDossier(), sent.getDateCreationDossier());
        assertEquals(dto.getDateModificationDossier(), sent.getDateModificationDossier());
        assertEquals(dto.getClient().getId(), sent.getClient().getId());
        assertEquals(dto.getVehicule().getId(), sent.getVehicule().getId());
        assertEquals(dto.getDossierStatus(), sent.getDossierStatus());
    }

    @Test
    void shouldCompleteCreationFutureWhenEventHandlerCallsComplete() {
        DossierCommandDTO request = sampleDossierCommandDto();
        CompletableFuture<DossierCommandDTO> result = service.createDossier(request);

        ArgumentCaptor<DossierCreateCommand> captor = ArgumentCaptor.forClass(DossierCreateCommand.class);
        verify(commandGateway, times(1)).send(captor.capture());
        String generatedId = captor.getValue().getId();

        DossierCommandDTO completion = sampleDossierCommandDto();
        completion.setId(generatedId);
        service.completeDossierCreation(completion);

        assertTrue(result.isDone());
        assertEquals(completion.getNomDossier(), result.join().getNomDossier());
    }

    @Test
    void shouldIgnoreCompleteWhenNoPendingFuture() {
        assertDoesNotThrow(() -> service.completeDossierCreation(sampleDossierCommandDto()));
    }
}

