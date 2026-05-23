package fr.cdrochon.smamonolithe.garage.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@AllArgsConstructor @NoArgsConstructor
@Schema(description = "Requête pour récupérer un garage spécifique par son ID")
public class GetGarageDTO {
    
    @Schema(description = "Identifiant unique du garage", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
}
