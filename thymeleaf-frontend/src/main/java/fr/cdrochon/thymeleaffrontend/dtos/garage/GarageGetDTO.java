package fr.cdrochon.thymeleaffrontend.dtos.garage;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarageGetDTO {
    private String id;
    private String nomGarage;
    private String mailResp;
    private GarageAdresseDTO adresse;
}
