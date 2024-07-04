package fr.cdrochon.smamonolithe.document.events;

import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatus;
import fr.cdrochon.smamonolithe.document.query.entities.TypeDocument;
import lombok.Getter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.Instant;
import java.time.LocalDate;

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
    private final TypeDocument typeDocument;
    private final String emetteurDuDocument;
    private final Instant dateCreationDocument;
    private final Instant dateModificationDocument;
    private final DocumentStatus documentStatus;
    
    public DocumentCreatedEvent(String id, String nomDocument, String titreDocument, TypeDocument typeDocument, String emetteurDuDocument,
                                Instant dateCreationDocument, Instant dateModificationDocument, DocumentStatus documentStatus) {
        super(id);
        this.nomDocument = nomDocument;
        this.titreDocument = titreDocument;
        this.typeDocument = typeDocument;
        this.emetteurDuDocument = emetteurDuDocument;
        this.dateCreationDocument = dateCreationDocument;
        this.dateModificationDocument = dateModificationDocument;
        this.documentStatus = documentStatus;
    }
    
}
