package fr.cdrochon.smamonolithe.document.query.dtos;

import fr.cdrochon.smamonolithe.document.common.dtos.DocumentBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO représentant un document dans une requête de lecture")
public class DocumentQueryDTO extends DocumentBaseDTO {

    // Champs communs factorisés dans DocumentBaseDTO

}
