package fr.cdrochon.thymeleaffrontend.dtos.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeThymDTO {
    
    private String id;
    
    @Pattern(regexp = "^[A-Z]{2}-\\d{3}-[A-Z]{2}$", message = "Le format requis doit etre de type AA-123-AA")
    private String immatriculationVehicule;
    private String dateMiseEnCirculationVehicule;
    private VehiculeStatusDTO vehiculeStatus;
    private ClientThymDTO client;
}
