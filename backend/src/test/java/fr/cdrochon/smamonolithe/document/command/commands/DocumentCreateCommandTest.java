package fr.cdrochon.smamonolithe.document.command.commands;

import fr.cdrochon.smamonolithe.document.DocumentTestDataFactory;
import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DocumentCreateCommandTest {

    @Test
    void shouldCreateCommandWithAllFields() {
        DocumentCreateCommand command = DocumentTestDataFactory.sampleCreateCommand();

        assertEquals("doc-1", command.getId());
        assertEquals("DOC-FACTURE-2026", command.getNomDocument());
        assertEquals("Facture Avril", command.getTitreDocument());
        assertEquals("Service Compta", command.getEmetteurDuDocument());
        assertEquals(DocumentTestDataFactory.sampleTypeDocument(), command.getTypeDocument());
        assertEquals(DocumentTestDataFactory.creationInstant(), command.getDateCreationDocument());
        assertEquals(DocumentTestDataFactory.modificationInstant(), command.getDateModificationDocument());
        assertEquals(DocumentStatusDTO.CREATED, command.getDocumentStatus());
    }

    @Test
    void shouldAllowNullOptionalFields() {
        DocumentCreateCommand command = new DocumentCreateCommand(
                "doc-2",
                "NAME",
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals("doc-2", command.getId());
        assertEquals("NAME", command.getNomDocument());
        assertNull(command.getTitreDocument());
        assertNull(command.getEmetteurDuDocument());
        assertNull(command.getTypeDocument());
        assertNull(command.getDateCreationDocument());
        assertNull(command.getDateModificationDocument());
        assertNull(command.getDocumentStatus());
    }

    @Test
    void shouldReturn500WithErrorMessageInExceptionHandler() {
        DocumentCreateCommand command = DocumentTestDataFactory.sampleCreateCommand();

        ResponseEntity<String> response = command.exceptionHandler(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("boom", response.getBody());
    }

    @Test
    void shouldPreserveDatesBetweenConstructorAndGetter() {
        DocumentCreateCommand cmd = DocumentTestDataFactory.sampleCreateCommand();
        assertEquals(DocumentTestDataFactory.creationInstant(), cmd.getDateCreationDocument());
        assertEquals(DocumentTestDataFactory.modificationInstant(), cmd.getDateModificationDocument());
    }

    @Test
    void shouldAllowCreationAndModificationDatesBeEqual() {
        java.time.Instant same = DocumentTestDataFactory.creationInstant();
        DocumentCreateCommand cmd = new DocumentCreateCommand(
                "doc-eq", "N", "T", "E",
                DocumentTestDataFactory.sampleTypeDocument(),
                same, same,
                DocumentStatusDTO.DRAFT
        );
        assertEquals(cmd.getDateCreationDocument(), cmd.getDateModificationDocument());
    }

    @Test
    void shouldSupportAllDocumentStatusValues() {
        for (DocumentStatusDTO status : DocumentStatusDTO.values()) {
            DocumentCreateCommand cmd = new DocumentCreateCommand(
                    "doc-s", "N", "T", "E",
                    DocumentTestDataFactory.sampleTypeDocument(),
                    DocumentTestDataFactory.creationInstant(),
                    DocumentTestDataFactory.modificationInstant(),
                    status
            );
            assertEquals(status, cmd.getDocumentStatus());
        }
    }

    @Test
    void shouldSupportUnicodeValues() {
        DocumentCreateCommand command = new DocumentCreateCommand(
                "doc-3",
                "DÖC-€",
                "Tîtré",
                "Émetteur",
                DocumentTestDataFactory.sampleTypeDocument(),
                DocumentTestDataFactory.creationInstant(),
                DocumentTestDataFactory.modificationInstant(),
                DocumentStatusDTO.ARCHIVED
        );

        assertEquals("DÖC-€", command.getNomDocument());
        assertEquals("Tîtré", command.getTitreDocument());
        assertEquals(DocumentStatusDTO.ARCHIVED, command.getDocumentStatus());
    }
}
