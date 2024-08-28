package fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymConvertDTO;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
//gere laprofondeur du json à cause de la relation bidirectionnelle
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class VehiculeThymConvertDTO {
    
    private String id;
    private String immatriculationVehicule;
    private Instant dateMiseEnCirculationVehicule;
    private VehiculeStatusDTO vehiculeStatus;
    private ClientThymConvertDTO client;
}
