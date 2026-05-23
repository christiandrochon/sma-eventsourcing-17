package fr.cdrochon.smamonolithe.client.query.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Requête pour récupérer un client spécifique par son ID")
public class GetClientDTO {
    
    @Schema(description = "Identifiant unique du client", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
}
