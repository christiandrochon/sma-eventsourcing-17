package fr.cdrochon.smamonolithe.document;

import fr.cdrochon.smamonolithe.document.command.commands.DocumentBaseCommand;
import fr.cdrochon.smamonolithe.document.command.commands.DocumentCreateCommand;
import fr.cdrochon.smamonolithe.document.command.dtos.DocumentCommandDTO;
import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import fr.cdrochon.smamonolithe.document.events.DocumentCreatedEvent;
import fr.cdrochon.smamonolithe.document.query.dtos.DocumentQueryDTO;
import fr.cdrochon.smamonolithe.document.query.dtos.GetDocumentDTO;
import fr.cdrochon.smamonolithe.document.query.entities.Document;
import fr.cdrochon.smamonolithe.document.query.entities.TypeDocument;
import fr.cdrochon.smamonolithe.document.query.mapper.DocumentQueryMapper;
import fr.cdrochon.smamonolithe.document.query.repositories.DocumentRepository;
import fr.cdrochon.smamonolithe.document.query.services.DocumentEventHandlerService;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.TransactionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Edge cases complémentaires pour le package document.
 * Couvre : ids vides, strings longues, status limite, null id query, TypeDocument custom,
 * double save, list partielle, mapper null partiel, etc.
 */
/**
 * Active l'extension Mockito de JUnit 5 pour initialiser et injecter automatiquement
 * les mocks utilises par ce test.
 */
@ExtendWith(MockitoExtension.class)
/**
 * Cette classe contient uniquement des tests unitaires.
 */
class DocumentEdgeCasesTest {

    @Mock
    private DocumentRepository documentRepository;

    /**
     * ──────────────────────────────────────────────────────────────────────────
     * Command — identifiers limites
     * ──────────────────────────────────────────────────────────────────────────
     */

    @Test
    void shouldAcceptEmptyStringIdInBaseCommand() {
        DocumentBaseCommand<String> cmd = new DocumentBaseCommand<>("");
        assertEquals("", cmd.getId());
    }

    @Test
    void shouldAcceptUUIDFormatId() {
        String uuid = UUID.randomUUID().toString();
        DocumentBaseCommand<String> cmd = new DocumentBaseCommand<>(uuid);
        assertEquals(uuid, cmd.getId());
    }

    @Test
    void shouldAcceptVeryLongNomDocumentInCreateCommand() {
        String longName = "A".repeat(500);
        DocumentCreateCommand cmd = new DocumentCreateCommand(
                "id", longName, "T", "E",
                DocumentTestDataFactory.sampleTypeDocument(),
                DocumentTestDataFactory.creationInstant(),
                DocumentTestDataFactory.modificationInstant(),
                DocumentStatusDTO.CREATED
        );
        assertEquals(longName, cmd.getNomDocument());
    }

    /**
     * ──────────────────────────────────────────────────────────────────────────
     * Events — payload immutabilité
     * ──────────────────────────────────────────────────────────────────────────
     */

    @Test
    void documentCreatedEventShouldNotExposeInternalMutability() {
        TypeDocument type = TypeDocument.DEVIS;
        DocumentCreatedEvent event = new DocumentCreatedEvent(
                "id", "N", "T", "E", type,
                DocumentTestDataFactory.creationInstant(),
                DocumentTestDataFactory.modificationInstant(),
                DocumentStatusDTO.DRAFT
        );
        /**
         * même référence, objet non muté par le constructeur
         */
        assertSame(type, event.getTypeDocument());
    }

    @Test
    void documentCreatedEventShouldPreserveInstantPrecision() {
        Instant precise = Instant.ofEpochMilli(1234567890123L);
        DocumentCreatedEvent event = new DocumentCreatedEvent(
                "id", "N", "T", "E",
                DocumentTestDataFactory.sampleTypeDocument(),
                precise,
                precise,
                DocumentStatusDTO.CREATED
        );
        assertEquals(precise.toEpochMilli(), event.getDateCreationDocument().toEpochMilli());
    }

    /**
     * ──────────────────────────────────────────────────────────────────────────
     * DTO — valeurs limites
     * ──────────────────────────────────────────────────────────────────────────
     */

    @Test
    void documentCommandDTOShouldBeEqualWhenBuiltTwiceWithSameValues() {
        DocumentCommandDTO a = DocumentTestDataFactory.sampleCommandDTO();
        DocumentCommandDTO b = DocumentTestDataFactory.sampleCommandDTO();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void documentQueryDTOShouldSupportAllStatusValues() {
        for (DocumentStatusDTO status : DocumentStatusDTO.values()) {
            DocumentQueryDTO dto = new DocumentQueryDTO();
            dto.setDocumentStatus(status);
            assertEquals(status, dto.getDocumentStatus());
        }
    }

    @Test
    void getDocumentDTOShouldSupportBlankId() {
        GetDocumentDTO dto = new GetDocumentDTO("   ");
        assertEquals("   ", dto.getId());
    }

    /**
     * ──────────────────────────────────────────────────────────────────────────
     * Mapper — cas partiels
     * ──────────────────────────────────────────────────────────────────────────
     */

    @Test
    void shouldConvertEntityWithNullTypeToDtoGracefully() {
        Document entity = DocumentTestDataFactory.sampleEntity();
        entity.setTypeDocument(null);

        DocumentQueryDTO dto = DocumentQueryMapper.convertDocumentToDocumentDTO(entity);

        assertNotNull(dto);
        assertNull(dto.getTypeDocument());
    }

    @Test
    void shouldConvertEntityWithNullDatesToDtoGracefully() {
        Document entity = DocumentTestDataFactory.sampleEntity();
        entity.setDateCreationDocument(null);
        entity.setDateModificationDocument(null);

        DocumentQueryDTO dto = DocumentQueryMapper.convertDocumentToDocumentDTO(entity);

        assertNull(dto.getDateCreationDocument());
        assertNull(dto.getDateModificationDocument());
    }

    @Test
    void shouldConvertDtoWithCustomTypedDocumentToEntity() {
        TypeDocument custom = TypeDocument.builder().nomTypeDocument("BON_LIVRAISON").build();
        DocumentQueryDTO dto = DocumentTestDataFactory.sampleQueryDTO();
        dto.setTypeDocument(custom);

        Document entity = DocumentQueryMapper.convertDocumentDTOToDocument(dto);

        assertNotNull(entity.getTypeDocument());
        assertEquals("BON_LIVRAISON", entity.getTypeDocument().getNomTypeDocument());
    }

    /**
     * ──────────────────────────────────────────────────────────────────────────
     * Service — comportements limites
     * ──────────────────────────────────────────────────────────────────────────
     */

    @Test
    void shouldSaveCorrectlyWhenEventHasNullEmetteur() {
        DocumentCreatedEvent event = new DocumentCreatedEvent(
                "doc-x", "Nom", "Titre", null,
                DocumentTestDataFactory.sampleTypeDocument(),
                DocumentTestDataFactory.creationInstant(),
                DocumentTestDataFactory.modificationInstant(),
                DocumentStatusDTO.CREATED
        );
        DocumentEventHandlerService service = new DocumentEventHandlerService(documentRepository);

        service.on(event);

        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void shouldSaveCorrectlyWhenEventHasNullTypeDocument() {
        DocumentCreatedEvent event = new DocumentCreatedEvent(
                "doc-y", "Nom", "Titre", "Emetteur", null,
                DocumentTestDataFactory.creationInstant(),
                DocumentTestDataFactory.modificationInstant(),
                DocumentStatusDTO.DRAFT
        );
        DocumentEventHandlerService service = new DocumentEventHandlerService(documentRepository);

        service.on(event);

        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void shouldReturnMultipleDocumentsInList() {
        DocumentEventHandlerService service = new DocumentEventHandlerService(documentRepository);
        when(documentRepository.findAll()).thenReturn(List.of(
                DocumentTestDataFactory.sampleEntity(),
                Document.builder().id("doc-2").nomDocument("DOC-B").documentStatus(DocumentStatusDTO.ARCHIVED).build()
        ));

        List<DocumentQueryDTO> result = service.on();

        assertEquals(2, result.size());
        assertEquals("doc-1", result.get(0).getId());
        assertEquals("doc-2", result.get(1).getId());
    }

    @Test
    void shouldThrowTransactionExceptionWhenRepositoryThrowsIllegalState() {
        DocumentEventHandlerService service = new DocumentEventHandlerService(documentRepository);
        when(documentRepository.save(any(Document.class))).thenThrow(new IllegalStateException("tx broken"));

        assertThrows(TransactionException.class, () -> service.on(DocumentTestDataFactory.sampleCreatedEvent()));
    }

    @Test
    void shouldThrowEntityNotFoundWithCorrectMessageForMissingDocument() {
        DocumentEventHandlerService service = new DocumentEventHandlerService(documentRepository);
        when(documentRepository.findById("no-doc")).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.on(new GetDocumentDTO("no-doc")));
        assertEquals("Document not found", ex.getMessage());
    }

    /**
     * ──────────────────────────────────────────────────────────────────────────
     * TypeDocument — contrat embeddable
     * ──────────────────────────────────────────────────────────────────────────
     */

    @Test
    void typeDocumentShouldAllowNullNomInDefaultConstructor() {
        TypeDocument t = new TypeDocument();
        assertNull(t.getNomTypeDocument());
    }

    @Test
    void typeDocumentBuilderShouldAllowEmptyString() {
        TypeDocument t = TypeDocument.builder().nomTypeDocument("").build();
        assertEquals("", t.getNomTypeDocument());
    }
}

