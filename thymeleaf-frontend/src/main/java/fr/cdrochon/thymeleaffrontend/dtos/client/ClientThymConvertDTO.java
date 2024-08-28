package fr.cdrochon.thymeleaffrontend.dtos.client;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymConvertDTO;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id") //gere la profondeur du json
public class ClientThymConvertDTO {
    
    private String id;
    
    private String nomClient;
    private String prenomClient;
    private String mailClient;
    private String telClient;
    private AdresseClientDTO adresse;
    private ClientStatusDTO clientStatus;
    private VehiculeThymConvertDTO vehicule;
    
}
