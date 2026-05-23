package fr.cdrochon.smamonolithe.garage.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @ToString
@Schema(description = "DTO représentant l'adresse d'un garage")
public class GarageAdresseDTO {
    @Schema(description = "Numéro de rue", example = "42")
    private String numeroDeRue;
    @Schema(description = "Nom de la rue", example = "avenue de l'Opéra")
    private String rue;
    @Schema(description = "Code postal", example = "75001")
    private String cp;
    @Schema(description = "Ville", example = "Paris")
    private String ville;
}
