package fr.cdrochon.thymeleaffrontend.dtos.vehicule;

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
}
