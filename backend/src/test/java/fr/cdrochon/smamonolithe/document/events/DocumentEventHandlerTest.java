package fr.cdrochon.smamonolithe.document.events;

import fr.cdrochon.smamonolithe.document.DocumentTestDataFactory;
import fr.cdrochon.smamonolithe.document.command.dtos.DocumentCommandDTO;
import fr.cdrochon.smamonolithe.document.command.services.DocumentCommandService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Active l'extension Mockito de JUnit 5 pour initialiser et injecter automatiquement
 * les mocks utilises par ce test.
 */
@ExtendWith(MockitoExtension.class)
/**
 * Cette classe contient uniquement des tests unitaires.
 */
class DocumentEventHandlerTest {

    @Mock
    private DocumentCommandService documentCommandService;

    @InjectMocks
    private DocumentEventHandler handler;

    @Test
    void shouldCompleteDocumentCreationWhenEventArrives() {
        handler.on(DocumentTestDataFactory.sampleCreatedEvent());

        verify(documentCommandService, times(1)).completeDocumentCreation(org.mockito.ArgumentMatchers.any(DocumentCommandDTO.class));
    }

    @Test
    void shouldMapIdToCommandDto() {
        ArgumentCaptor<DocumentCommandDTO> captor = ArgumentCaptor.forClass(DocumentCommandDTO.class);

        handler.on(DocumentTestDataFactory.sampleCreatedEvent());

        verify(documentCommandService).completeDocumentCreation(captor.capture());
        assertEquals("doc-1", captor.getValue().getId());
    }

    @Test
    void shouldMapTitleToCommandDto() {
        ArgumentCaptor<DocumentCommandDTO> captor = ArgumentCaptor.forClass(DocumentCommandDTO.class);

        handler.on(DocumentTestDataFactory.sampleCreatedEvent());

        verify(documentCommandService).completeDocumentCreation(captor.capture());
        assertEquals("Facture Avril", captor.getValue().getTitreDocument());
    }

    @Test
    void shouldMapTypeToCommandDto() {
        ArgumentCaptor<DocumentCommandDTO> captor = ArgumentCaptor.forClass(DocumentCommandDTO.class);

        handler.on(DocumentTestDataFactory.sampleCreatedEvent());

        verify(documentCommandService).completeDocumentCreation(captor.capture());
        assertNotNull(captor.getValue().getTypeDocument());
    }

    @Test
    void shouldMapStatusToCommandDto() {
        ArgumentCaptor<DocumentCommandDTO> captor = ArgumentCaptor.forClass(DocumentCommandDTO.class);

        handler.on(DocumentTestDataFactory.sampleCreatedEvent());

        verify(documentCommandService).completeDocumentCreation(captor.capture());
        assertNotNull(captor.getValue().getDocumentStatus());
    }
}
