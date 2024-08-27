package fr.cdrochon.thymeleaffrontend.dtos.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymConvertDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymDTO;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeThymConvertDTO {
    
    private String id;
    private String immatriculationVehicule;
    private Instant dateMiseEnCirculationVehicule;
    private VehiculeStatusDTO vehiculeStatus;
    private ClientThymConvertDTO client;
}
