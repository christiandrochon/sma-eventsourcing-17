package fr.cdrochon.thymeleaffrontend.dtos.client;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymConvertDTO;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
