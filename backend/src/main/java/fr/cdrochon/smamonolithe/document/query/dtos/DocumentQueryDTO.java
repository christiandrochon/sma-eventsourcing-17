package fr.cdrochon.smamonolithe.document.query.dtos;

import fr.cdrochon.smamonolithe.document.common.dtos.DocumentBaseDTO;
import fr.cdrochon.smamonolithe.document.query.entities.TypeDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Schema(description = "DTO représentant un document dans une requête de lecture")
public class DocumentQueryDTO extends DocumentBaseDTO {


}
