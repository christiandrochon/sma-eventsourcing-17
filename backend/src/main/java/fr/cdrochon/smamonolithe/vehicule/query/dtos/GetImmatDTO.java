package fr.cdrochon.smamonolithe.vehicule.query.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO pour rechercher un véhicule par immatriculation")
public class GetImmatDTO {
    
    @Schema(description = "Immatriculation du véhicule à rechercher", example = "AB-123-CD")
    private String immatriculation;
}
