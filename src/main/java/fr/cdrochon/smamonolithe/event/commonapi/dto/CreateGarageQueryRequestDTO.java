package fr.cdrochon.smamonolithe.event.commonapi.dto;

import fr.cdrochon.smamonolithe.event.commonapi.enums.GarageStatus;
import lombok.*;

/**
 * permet de faire le lien entre les services de l'appli et le monde exteieur
 */
@Data
@NoArgsConstructor @AllArgsConstructor @Builder
public class CreateGarageQueryRequestDTO {

    private String nomClient;
    private String mailResponsable;
    private GarageStatus garageStatus;
}
