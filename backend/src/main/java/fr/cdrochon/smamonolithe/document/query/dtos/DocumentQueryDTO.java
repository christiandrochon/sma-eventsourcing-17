package fr.cdrochon.smamonolithe.document.query.dtos;

import fr.cdrochon.smamonolithe.document.common.dtos.DocumentBaseDTO;
import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import fr.cdrochon.smamonolithe.document.query.entities.TypeDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Schema(description = "DTO représentant un document dans une requête de lecture")
public class DocumentQueryDTO extends DocumentBaseDTO {

    // Champs communs factorisés dans DocumentBaseDTO

    // Explicit constructors to avoid Lombok duplicate-constructor issues in some build environments
    public DocumentQueryDTO() {
        super();
    }

    public DocumentQueryDTO(String id,
                            String nomDocument,
                            String titreDocument,
                            String emetteurDuDocument,
                            TypeDocument typeDocument,
                            Instant dateCreationDocument,
                            Instant dateModificationDocument,
                            DocumentStatusDTO documentStatus) {
        super(id, nomDocument, titreDocument, emetteurDuDocument, typeDocument,
                dateCreationDocument, dateModificationDocument, documentStatus);
    }

}
