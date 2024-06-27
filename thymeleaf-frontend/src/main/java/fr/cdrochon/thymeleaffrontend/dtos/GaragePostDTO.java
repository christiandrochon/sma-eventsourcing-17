package fr.cdrochon.thymeleaffrontend.dtos;

import fr.cdrochon.thymeleaffrontend.entity.AdresseGarage;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
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
    private  AdresseGarage adresse;
}
