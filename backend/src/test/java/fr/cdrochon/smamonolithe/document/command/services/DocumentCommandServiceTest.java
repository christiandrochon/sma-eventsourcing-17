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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Active l'extension Mockito de JUnit 5 pour initialiser et injecter automatiquement
 * les mocks utilises par ce test.
 */
@ExtendWith(MockitoExtension.class)
class DocumentCommandServiceTest {

    @Mock
    private CommandGateway commandGateway;

    private DocumentCommandService service;

    @BeforeEach
    void setUp() {
        service = new DocumentCommandService(commandGateway);
        lenient().when(commandGateway.send(any())).thenReturn(CompletableFuture.completedFuture(null));
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
        assertTrue(future.isDone());
    }

    @Test
    void shouldCompleteFutureWhenEventHandlerCallbackInvoked() {
        CompletableFuture<DocumentCommandDTO> future = service.createDocument(DocumentTestDataFactory.sampleCommandDTO());
        DocumentCommandDTO dto = DocumentTestDataFactory.sampleCommandDTO();

        service.completeDocumentCreation(dto);

        assertTrue(future.isDone());
        assertEquals(dto.getNomDocument(), future.join().getNomDocument());
        assertNotNull(future.join().getId());
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
        /**
         * première future remplacée
         */
        service.createDocument(DocumentTestDataFactory.sampleCommandDTO());
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
