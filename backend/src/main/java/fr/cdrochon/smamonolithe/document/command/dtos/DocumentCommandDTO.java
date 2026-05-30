package fr.cdrochon.smamonolithe.document.command.dtos;

import fr.cdrochon.smamonolithe.document.common.dtos.DocumentBaseDTO;
import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import fr.cdrochon.smamonolithe.document.query.entities.TypeDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Schema(description = "DTO utilisé pour les commandes liées aux documents")
public class DocumentCommandDTO extends DocumentBaseDTO {

    @Schema(description = "Identifiant du client propriétaire du document", example = "550e8400-e29b-41d4-a716-446655440000")
    private String clientId;


}
