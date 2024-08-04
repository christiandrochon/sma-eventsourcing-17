package fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner;

import lombok.*;

import java.time.Instant;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeFromServerDTO {
    private String idVehicule;
    private String immatriculationVehicule;
    private Instant dateMiseEnCirculationVehicule;
    private VehiculeStatusDTO vehiculeStatus;
}
