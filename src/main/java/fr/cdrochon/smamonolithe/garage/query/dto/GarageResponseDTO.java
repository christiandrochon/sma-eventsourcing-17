package fr.cdrochon.smamonolithe.garage.query.dto;

import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor @ToString
public class GarageResponseDTO {
    
    private String id;
    private String nomGarage;
    private String mailResp;
    private GarageAdresseDTO adresse;
}
