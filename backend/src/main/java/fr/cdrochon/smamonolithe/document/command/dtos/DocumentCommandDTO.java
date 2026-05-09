package fr.cdrochon.smamonolithe.document.command.dtos;

import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import fr.cdrochon.smamonolithe.document.query.entities.TypeDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DocumentCommandDTO {
    
    private String id;
    private String nomDocument;
    private String titreDocument;
    private String emetteurDuDocument;
    private TypeDocument typeDocument;
    private Instant dateCreationDocument;
    private Instant dateModificationDocument;
    private DocumentStatusDTO documentStatus;
}
