package fr.cdrochon.thymeleaffrontend.dtos.garage;

import fr.cdrochon.thymeleaffrontend.entity.garage.AdresseGarage;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GaragePostDTO implements Serializable {
    private String id;
    private String nomGarage;
    private String mailResp;
    //FIXME : utiliser l'adresse DTO ?
    private  AdresseGarage adresse;
}
