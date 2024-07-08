package fr.cdrochon.thymeleaffrontend.dtos.vehicule;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeDateConvertDTO {
    
    private String idVehicule;
    private String immatriculationVehicule;
    private Instant dateMiseEnCirculationVehicule;
    private VehiculeStatusDTO vehiculeStatus;
}
