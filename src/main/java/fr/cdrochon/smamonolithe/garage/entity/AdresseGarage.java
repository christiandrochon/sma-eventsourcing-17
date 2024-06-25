package fr.cdrochon.smamonolithe.garage.entity;


import lombok.*;

import javax.persistence.Embeddable;


@Embeddable
@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class AdresseGarage {
    
    private String numeroDeRue;
    private String rue;
    private String cp;
    private String ville;
}
