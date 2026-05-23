package fr.cdrochon.smamonolithe.garage.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor @ToString
@Schema(description = "DTO représentant un garage dans une requête de lecture")
public class GarageQueryDTO {
    
    @Schema(description = "Identifiant unique du garage", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    @Schema(description = "Nom du garage", example = "Garage Central Paris")
    private String nomGarage;
    @Schema(description = "Email du responsable du garage", example = "contact@garage-central.com")
    private String mailResp;
    @Schema(description = "Adresse du garage")
    private GarageAdresseDTO adresse;
}
