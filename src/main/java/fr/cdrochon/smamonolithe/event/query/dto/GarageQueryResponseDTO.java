package fr.cdrochon.smamonolithe.event.query.dto;

import lombok.*;

@Data
@AllArgsConstructor @NoArgsConstructor
public class GarageQueryResponseDTO {
    private String id;
    private String nomGarage;
    private String mailResp;
}
