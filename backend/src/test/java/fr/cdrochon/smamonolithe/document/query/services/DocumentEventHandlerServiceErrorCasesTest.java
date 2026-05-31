package fr.cdrochon.smamonolithe.document.query.services;

import fr.cdrochon.smamonolithe.document.DocumentTestDataFactory;
import fr.cdrochon.smamonolithe.document.query.dtos.GetDocumentDTO;
import fr.cdrochon.smamonolithe.document.query.entities.Document;
import fr.cdrochon.smamonolithe.document.query.repositories.DocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.TransactionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Active l'extension Mockito de JUnit 5 pour initialiser et injecter automatiquement
 * les mocks utilises par ce test.
 */
@ExtendWith(MockitoExtension.class)
/**
 * Cette classe contient uniquement des tests unitaires.
 */
class DocumentEventHandlerServiceErrorCasesTest {

    @Mock
    private DocumentRepository documentRepository;

    @Test
    void shouldWrapPersistenceErrorInTransactionException() {
        DocumentEventHandlerService service = new DocumentEventHandlerService(documentRepository);
        when(documentRepository.save(any(Document.class))).thenThrow(new RuntimeException("db down"));

        TransactionException exception = assertThrows(TransactionException.class,
                () -> service.on(DocumentTestDataFactory.sampleCreatedEvent()));

        assertTrue(exception.getMessage().contains("sauvegarde du document"));
    }

    @Test
    void shouldThrowEntityNotFoundForUnknownId() {
        DocumentEventHandlerService service = new DocumentEventHandlerService(documentRepository);
        when(documentRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.on(new GetDocumentDTO("unknown")));
    }

    @Test
    void shouldThrowEntityNotFoundForNullId() {
        DocumentEventHandlerService service = new DocumentEventHandlerService(documentRepository);
        when(documentRepository.findById(null)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.on(new GetDocumentDTO(null)));
    }

    @Test
    void shouldThrowTransactionExceptionForConstraintViolation() {
        DocumentEventHandlerService service = new DocumentEventHandlerService(documentRepository);
        when(documentRepository.save(any(Document.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate"));

        assertThrows(TransactionException.class, () -> service.on(DocumentTestDataFactory.sampleCreatedEvent()));
    }
}
