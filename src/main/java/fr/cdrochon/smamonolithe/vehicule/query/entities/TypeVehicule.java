package fr.cdrochon.smamonolithe.vehicule.query.entities;

import lombok.*;

import javax.persistence.Embeddable;
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
    
    public Collection<String> getTypeVehicule() {
        return typeVehicule;
    }
    
    public void setTypeVehicule(Collection<String> typeVehicule) {
        this.typeVehicule = typeVehicule;
    }
}
