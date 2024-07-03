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
public class TypeVehicule {
    
    Collection<String> typeVehicule = Arrays.asList("VOITURE", "CAMIONNETTE", "MOTO", "TRICYCLE", "CAMPING_CAR", "NON_DISPONIBLE");
    
}
