package fr.cdrochon.smamonolithe.document.query.services;

import fr.cdrochon.smamonolithe.document.DocumentTestDataFactory;
import fr.cdrochon.smamonolithe.document.events.DocumentCreatedEvent;
import fr.cdrochon.smamonolithe.document.query.dtos.DocumentQueryDTO;
import fr.cdrochon.smamonolithe.document.query.dtos.GetDocumentDTO;
import fr.cdrochon.smamonolithe.document.query.entities.Document;
import fr.cdrochon.smamonolithe.document.query.repositories.DocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentEventHandlerServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    private DocumentEventHandlerService service;

    @BeforeEach
    void setUp() {
        service = new DocumentEventHandlerService(documentRepository);
    }

    @Test
    void shouldSaveDocumentWhenCreatedEventReceived() {
        service.on(DocumentTestDataFactory.sampleCreatedEvent());

        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void shouldMapEventFieldsToEntityBeforeSave() {
        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);

        service.on(DocumentTestDataFactory.sampleCreatedEvent());

        verify(documentRepository).save(captor.capture());
        Document saved = captor.getValue();
        assertEquals("doc-1", saved.getId());
        assertEquals("DOC-FACTURE-2026", saved.getNomDocument());
        assertEquals("Facture Avril", saved.getTitreDocument());
        assertEquals("Service Compta", saved.getEmetteurDuDocument());
        assertEquals(DocumentTestDataFactory.sampleTypeDocument(), saved.getTypeDocument());
        assertEquals(DocumentTestDataFactory.creationInstant(), saved.getDateCreationDocument());
        assertEquals(DocumentTestDataFactory.modificationInstant(), saved.getDateModificationDocument());
        assertNotNull(saved.getDocumentStatus());
    }

    @Test
    void shouldReturnDocumentByIdWhenFound() {
        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(DocumentTestDataFactory.sampleEntity()));

        DocumentQueryDTO result = service.on(new GetDocumentDTO("doc-1"));

        assertNotNull(result);
        assertEquals("doc-1", result.getId());
    }

    @Test
    void shouldThrowEntityNotFoundWhenDocumentMissing() {
        when(documentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.on(new GetDocumentDTO("missing")));
    }

    @Test
    void shouldReturnAllDocumentsMappedToDtos() {
        when(documentRepository.findAll()).thenReturn(List.of(DocumentTestDataFactory.sampleEntity()));

        List<DocumentQueryDTO> result = service.on();

        assertEquals(1, result.size());
        assertEquals("doc-1", result.get(0).getId());
    }

    @Test
    void shouldMapMultipleEventsIndependently() {
        DocumentCreatedEvent event2 = new fr.cdrochon.smamonolithe.document.events.DocumentCreatedEvent(
                "doc-2", "OTHER", null, null,
                DocumentTestDataFactory.sampleTypeDocument(),
                DocumentTestDataFactory.creationInstant(),
                DocumentTestDataFactory.modificationInstant(),
                fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO.ARCHIVED
        );

        service.on(DocumentTestDataFactory.sampleCreatedEvent());
        service.on(event2);

        verify(documentRepository, times(2)).save(any(Document.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoDocuments() {
        when(documentRepository.findAll()).thenReturn(Collections.emptyList());

        List<DocumentQueryDTO> result = service.on();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
