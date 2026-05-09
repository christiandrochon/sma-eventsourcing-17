package fr.cdrochon.smamonolithe.document.query.dtos;

import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import fr.cdrochon.smamonolithe.document.query.entities.TypeDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentQueryDTO {
    
    private String id;
    private String nomDocument;
    private String titreDocument;
    private String emetteurDuDocument;
    private TypeDocument typeDocument;
    private Instant dateCreationDocument;
    private Instant dateModificationDocument;
    private DocumentStatusDTO documentStatus;
}
