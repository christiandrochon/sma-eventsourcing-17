package fr.cdrochon.smamonolithe.vehicule.query.entities;

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
public class TypeBoiteVitesse {
    
    Collection<String> typeBoiteVitesse = Arrays.asList("MANUELLE", "AUTOMATIQUE", "ROBOTISEE");
    
}
