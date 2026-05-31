package fr.cdrochon.smamonolithe.document.query.controllers;

import fr.cdrochon.smamonolithe.document.DocumentTestDataFactory;
import fr.cdrochon.smamonolithe.document.query.entities.Document;
import fr.cdrochon.smamonolithe.document.query.repositories.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Active l'extension Mockito de JUnit 5 pour initialiser et injecter automatiquement
 * les mocks utilises par ce test.
 */
@ExtendWith(MockitoExtension.class)
class DocumentQueryControllerTest {

    @Mock
    private DocumentRepository documentRepository;

    private DocumentQueryController controller;

    @BeforeEach
    void setUp() {
        controller = new DocumentQueryController(documentRepository);
    }

    @Test
    void shouldReturnDocumentMonoWhenFound() {
        Document entity = DocumentTestDataFactory.sampleEntity();
        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(entity));

        StepVerifier.create(controller.getDocumentByIdAsync("doc-1"))
                .assertNext(dto -> {
                    assertEquals("doc-1", dto.getId());
                    assertEquals("DOC-FACTURE-2026", dto.getNomDocument());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyMonoWhenDocumentNotFound() {
        when(documentRepository.findById("missing")).thenReturn(Optional.empty());

        StepVerifier.create(controller.getDocumentByIdAsync("missing"))
                .verifyComplete();
    }

    @Test
    void shouldReturnFluxWithAllDocuments() {
        when(documentRepository.findAll()).thenReturn(List.of(DocumentTestDataFactory.sampleEntity()));

        StepVerifier.create(controller.getDossiersAsync())
                .assertNext(dto -> assertEquals("doc-1", dto.getId()))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyFluxWhenNoDocuments() {
        when(documentRepository.findAll()).thenReturn(List.of());

        StepVerifier.create(controller.getDossiersAsync())
                .verifyComplete();
    }
}
