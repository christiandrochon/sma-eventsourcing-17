package fr.cdrochon.smamonolithe.garage.query.dto;

import lombok.*;

import javax.persistence.Embeddable;



@Getter
@Setter
@NoArgsConstructor
public class GarageAdresseDTO {
    private String numeroDeRue;
    private String rue;
    private String cp;
    private String ville;
}
