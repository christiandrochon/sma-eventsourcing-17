package fr.cdrochon.smamonolithe.garage.command.dtos;

import fr.cdrochon.smamonolithe.garage.query.entities.AdresseGarage;
import lombok.*;

/**
 * permet de faire le lien entre les services command (et query ??) de l'appli et le monde exteieur
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GarageRequestDTO {
//    private String id;
    private String nomGarage;
    private String mailResponsable;
    private AdresseGarage adresseGarage;
//    private GarageStatus garageStatus;
//    private Instant dateQuery;
}
