package fr.cdrochon.smamonolithe.garage.query.dto;

import fr.cdrochon.smamonolithe.garage.command.enums.GarageStatus;
import lombok.*;

@Data
@AllArgsConstructor @NoArgsConstructor
public class GarageResponseDTO {
    private String id;
    private String nomGarage;
    private String mailResp;
    private GarageStatus garageStatus;
}
