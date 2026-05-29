package fr.cdrochon.smamonolithe.document.command.dtos;

import fr.cdrochon.smamonolithe.document.common.dtos.DocumentBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "DTO utilisé pour les commandes liées aux documents")
public class DocumentCommandDTO extends DocumentBaseDTO {

    @Schema(description = "Identifiant du client propriétaire du document", example = "550e8400-e29b-41d4-a716-446655440000")
    private String clientId;

    public DocumentCommandDTO(String id,
                              String nomDocument,
                              String titreDocument,
                              String emetteurDuDocument,
                              fr.cdrochon.smamonolithe.document.query.entities.TypeDocument typeDocument,
                              java.time.Instant dateCreationDocument,
                              java.time.Instant dateModificationDocument,
                              fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO documentStatus) {
        super(id, nomDocument, titreDocument, emetteurDuDocument, typeDocument, dateCreationDocument, dateModificationDocument, documentStatus);
        this.clientId = null;
    }

}
