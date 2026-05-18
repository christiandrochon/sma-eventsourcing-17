package fr.cdrochon.smamonolithe.document.events;

import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import fr.cdrochon.smamonolithe.document.query.entities.TypeDocument;
import lombok.Getter;

import java.time.Instant;

/**
 * Les events sont exprimés dans le passé (pour le nommage).
 * Chaque event possede un id.
 * <p>
 * Objet immutable
 */
@Getter
public class DocumentCreatedEvent extends DocumentBaseEvent<String> {
    
    private final String nomDocument;
    private final String titreDocument;
    private final String emetteurDuDocument;
    private final TypeDocument typeDocument;
    private final Instant dateCreationDocument;
    private final Instant dateModificationDocument;
    private final DocumentStatusDTO documentStatus;
    private final String clientId;

    public DocumentCreatedEvent(String id, String nomDocument, String titreDocument, String emetteurDuDocument, TypeDocument typeDocument,
                                Instant dateCreationDocument, Instant  dateModificationDocument, DocumentStatusDTO documentStatus,
                                String clientId) {
        super(id);
        this.nomDocument = nomDocument;
        this.titreDocument = titreDocument;
        this.emetteurDuDocument = emetteurDuDocument;
        this.typeDocument = typeDocument;
        this.dateCreationDocument = dateCreationDocument;
        this.dateModificationDocument = dateModificationDocument;
        this.documentStatus = documentStatus;
        this.clientId = clientId;
    }

    public DocumentCreatedEvent(String id,
                                String nomDocument,
                                String titreDocument,
                                String emetteurDuDocument,
                                TypeDocument typeDocument,
                                Instant dateCreationDocument,
                                Instant dateModificationDocument,
                                DocumentStatusDTO documentStatus) {
        this(id, nomDocument, titreDocument, emetteurDuDocument, typeDocument,
                dateCreationDocument, dateModificationDocument, documentStatus, null);
    }


}
