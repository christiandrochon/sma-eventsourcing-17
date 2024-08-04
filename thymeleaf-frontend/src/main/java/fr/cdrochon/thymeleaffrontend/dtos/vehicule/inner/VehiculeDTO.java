package fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeDTO {
    
    private String idVehicule;
    private String immatriculationVehicule;
    private String dateMiseEnCirculationVehicule;
    private String vehiculeStatus;
}
