package fr.cdrochon.thymeleaffrontend.entity.vehicule;

import jakarta.persistence.Embeddable;
import lombok.*;


import java.util.Arrays;
import java.util.Collection;
@Embeddable
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TypePropulsion {
    
    Collection<String> typePropulsion = Arrays.asList("MOTEUR_A_L_AVANT", "MOTEUR_A_L_ARRIERE", "MOTEUR_AVANT_ET_ARRIERE");

}
