package fr.cdrochon.thymeleaffrontend.entity.garage;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdresseGarage {

    private String numeroDeRue;
    private String rue;
    private String cp;
    private String ville;
}
