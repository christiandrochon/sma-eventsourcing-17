package fr.cdrochon.smamonolithe.document.command.commands;

import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatus;
import fr.cdrochon.smamonolithe.document.query.entities.TypeDocument;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

/**
 * Classe abstraite immutable qui effectue une command
 * Implemente l'ajout d'un document via un event sur le CommandBus
 * <p>
 * Chaque command possede un id
 */
@Getter
public class DocumentCreateCommand extends DocumentBaseCommand<String> {
    
    private final String nomDocument;
    private final String titreDocument;
    private final String emetteurDuDocument;
    private final TypeDocument typeDocument;
    private final Instant dateCreationDocument;
    private final Instant dateModificationDocument;
    private final DocumentStatus documentStatus;
    
    public DocumentCreateCommand(String id, String nomDocument, String titreDocument, String emetteurDuDocument, TypeDocument typeDocument,
                                 Instant dateCreationDocument, Instant dateModificationDocument, DocumentStatus documentStatus) {
        super(id);
        this.nomDocument = nomDocument;
        this.titreDocument = titreDocument;
        this.emetteurDuDocument = emetteurDuDocument;
        this.typeDocument = typeDocument;
        this.dateCreationDocument = dateCreationDocument;
        this.dateModificationDocument = dateModificationDocument;
        this.documentStatus = documentStatus;
    }
    
    /**
     * Capture n'importe quelle exception en interne et affiche son message
     *
     * @param exception exception
     * @return ResponseEntity<String>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    
}
