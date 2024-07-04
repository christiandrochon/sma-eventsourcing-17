package fr.cdrochon.smamonolithe.document.query.dtos;

import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatus;
import fr.cdrochon.smamonolithe.document.query.entities.TypeDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Enumerated;
import java.time.Instant;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponseDTO {
    
    private String idDocument;
    private String nomDocument;
    private String titreDocument;
    private String emetteurDuDocument;
    private TypeDocument typeDocument;
    private Instant dateCreationDocument;
    private Instant dateModificationDocument;
    private DocumentStatus documentStatus;
}
