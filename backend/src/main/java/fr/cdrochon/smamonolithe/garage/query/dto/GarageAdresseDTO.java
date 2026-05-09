package fr.cdrochon.smamonolithe.garage.query.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @ToString
public class GarageAdresseDTO {
    private String numeroDeRue;
    private String rue;
    private String cp;
    private String ville;
}
