package fr.cdrochon.smamonolithe.event.query.dto;

import fr.cdrochon.smamonolithe.event.commonapi.enums.GarageStatus;
import lombok.*;

@Data
@AllArgsConstructor @NoArgsConstructor
public class GarageQueryResponseDTO {
    private String id;
    private String nomGarage;
    private String mailResp;
    private GarageStatus garageStatus;
}
