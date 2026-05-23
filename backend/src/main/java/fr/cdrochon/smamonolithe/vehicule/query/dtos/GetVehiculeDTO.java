package fr.cdrochon.smamonolithe.vehicule.query.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO pour récupérer un véhicule par ID")
public class GetVehiculeDTO {
    
    @Schema(description = "Identifiant du véhicule à récupérer", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
}
