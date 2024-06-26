package fr.cdrochon.thymeleaffrontend.dtos;

import fr.cdrochon.thymeleaffrontend.entity.Adresse;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarageDTO {
    private String id;
    private String nomGarage;
    private String mailResp;
    private Adresse adresse;
    
}
