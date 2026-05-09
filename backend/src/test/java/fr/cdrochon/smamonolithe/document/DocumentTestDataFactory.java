package fr.cdrochon.smamonolithe.document;

import fr.cdrochon.smamonolithe.document.command.commands.DocumentCreateCommand;
import fr.cdrochon.smamonolithe.document.command.dtos.DocumentCommandDTO;
import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import fr.cdrochon.smamonolithe.document.events.DocumentCreatedEvent;
import fr.cdrochon.smamonolithe.document.query.dtos.DocumentQueryDTO;
import fr.cdrochon.smamonolithe.document.query.dtos.GetDocumentDTO;
import fr.cdrochon.smamonolithe.document.query.entities.Document;
import fr.cdrochon.smamonolithe.document.query.entities.TypeDocument;

import java.time.Instant;

public final class DocumentTestDataFactory {

    private DocumentTestDataFactory() {
    }

    public static Instant creationInstant() {
        return Instant.parse("2026-01-10T10:15:30Z");
    }

    public static Instant modificationInstant() {
        return Instant.parse("2026-01-12T11:45:30Z");
    }

    public static TypeDocument sampleTypeDocument() {
        return TypeDocument.FACTURE;
    }

    public static DocumentCreateCommand sampleCreateCommand() {
        return new DocumentCreateCommand(
                "doc-1",
                "DOC-FACTURE-2026",
                "Facture Avril",
                "Service Compta",
                sampleTypeDocument(),
                creationInstant(),
                modificationInstant(),
                DocumentStatusDTO.CREATED
        );
    }

    public static DocumentCommandDTO sampleCommandDTO() {
        return DocumentCommandDTO.builder()
                .id("doc-1")
                .nomDocument("DOC-FACTURE-2026")
                .titreDocument("Facture Avril")
                .emetteurDuDocument("Service Compta")
                .typeDocument(sampleTypeDocument())
                .dateCreationDocument(creationInstant())
                .dateModificationDocument(modificationInstant())
                .documentStatus(DocumentStatusDTO.CREATED)
                .build();
    }

    public static DocumentCreatedEvent sampleCreatedEvent() {
        return new DocumentCreatedEvent(
                "doc-1",
                "DOC-FACTURE-2026",
                "Facture Avril",
                "Service Compta",
                sampleTypeDocument(),
                creationInstant(),
                modificationInstant(),
                DocumentStatusDTO.CREATED
        );
    }

    public static Document sampleEntity() {
        return Document.builder()
                .id("doc-1")
                .nomDocument("DOC-FACTURE-2026")
                .titreDocument("Facture Avril")
                .emetteurDuDocument("Service Compta")
                .typeDocument(sampleTypeDocument())
                .dateCreationDocument(creationInstant())
                .dateModificationDocument(modificationInstant())
                .documentStatus(DocumentStatusDTO.CREATED)
                .build();
    }

    public static DocumentQueryDTO sampleQueryDTO() {
        return new DocumentQueryDTO(
                "doc-1",
                "DOC-FACTURE-2026",
                "Facture Avril",
                "Service Compta",
                sampleTypeDocument(),
                creationInstant(),
                modificationInstant(),
                DocumentStatusDTO.CREATED
        );
    }

    public static GetDocumentDTO sampleGetDocumentDTO() {
        return new GetDocumentDTO("doc-1");
    }
}
