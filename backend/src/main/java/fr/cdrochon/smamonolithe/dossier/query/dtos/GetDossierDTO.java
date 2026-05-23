package fr.cdrochon.smamonolithe.dossier.query.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Requête pour récupérer un dossier spécifique par son ID")
public class GetDossierDTO {
    
    @Schema(description = "Identifiant unique du dossier", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
}
