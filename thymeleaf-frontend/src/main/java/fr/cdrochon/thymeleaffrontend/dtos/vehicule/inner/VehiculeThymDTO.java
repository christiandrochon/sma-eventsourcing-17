package fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeThymDTO {
    
    private String id;
    @NotBlank(message = "L'immatriculation du véhicule est obligatoire")
    private String immatriculationVehicule;
    @NotBlank(message = "La date de mise en circulation du véhicule est obligatoire")
    private String dateMiseEnCirculationVehicule;
    private String vehiculeStatus;
    @Valid
    private ClientThymDTO client;
}
