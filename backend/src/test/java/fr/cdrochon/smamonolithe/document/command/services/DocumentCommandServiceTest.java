package fr.cdrochon.smamonolithe.document.command.services;

import fr.cdrochon.smamonolithe.document.DocumentTestDataFactory;
import fr.cdrochon.smamonolithe.document.command.dtos.DocumentCommandDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DocumentCommandServiceTest {

    @Mock
    private CommandGateway commandGateway;

    private DocumentCommandService service;

    @BeforeEach
    void setUp() {
        service = new DocumentCommandService(commandGateway);
    }

    @Test
    void shouldSendCreateCommandUsingGateway() {
        service.createDocument(DocumentTestDataFactory.sampleCommandDTO());

        verify(commandGateway, times(1)).send(any());
    }

    @Test
    void shouldReturnNonCompletedFutureOnCreate() {
        CompletableFuture<DocumentCommandDTO> future = service.createDocument(DocumentTestDataFactory.sampleCommandDTO());

        assertNotNull(future);
        assertFalse(future.isDone());
    }

    @Test
    void shouldCompleteFutureWhenEventHandlerCallbackInvoked() {
        CompletableFuture<DocumentCommandDTO> future = service.createDocument(DocumentTestDataFactory.sampleCommandDTO());
        DocumentCommandDTO dto = DocumentTestDataFactory.sampleCommandDTO();

        service.completeDocumentCreation(dto);

        assertTrue(future.isDone());
        assertEquals(dto, future.join());
    }

    @Test
    void shouldIgnoreCompletionWhenFutureNotInitialized() {
        assertDoesNotThrow(() -> service.completeDocumentCreation(DocumentTestDataFactory.sampleCommandDTO()));
    }

    @Test
    void shouldCreateDifferentFutureForEachCreateCall() {
        CompletableFuture<DocumentCommandDTO> future1 = service.createDocument(DocumentTestDataFactory.sampleCommandDTO());
        CompletableFuture<DocumentCommandDTO> future2 = service.createDocument(DocumentTestDataFactory.sampleCommandDTO());

        assertNotSame(future1, future2);
    }

    @Test
    void shouldStillCompleteSecondFutureAfterFirstReplaced() {
        service.createDocument(DocumentTestDataFactory.sampleCommandDTO()); // première future remplacée
        CompletableFuture<DocumentCommandDTO> secondFuture = service.createDocument(DocumentTestDataFactory.sampleCommandDTO());
        service.completeDocumentCreation(DocumentTestDataFactory.sampleCommandDTO());
        assertTrue(secondFuture.isDone());
    }

    @Test
    void shouldAcceptDtoWithNullFieldsAtServiceLevel() {
        DocumentCommandDTO dto = new DocumentCommandDTO();
        dto.setNomDocument(null);

        assertDoesNotThrow(() -> service.createDocument(dto));
        verify(commandGateway, times(1)).send(any());
    }
}
