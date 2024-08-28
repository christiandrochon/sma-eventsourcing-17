package fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymDTO;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeThymDTO {
    
    private String id;
    private String immatriculationVehicule;
    private String dateMiseEnCirculationVehicule;
    private String vehiculeStatus;
    private ClientThymDTO client;
}
