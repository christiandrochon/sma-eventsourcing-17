package fr.cdrochon.smamonolithe.client.query.dtos;

import fr.cdrochon.smamonolithe.client.query.entities.ClientStatus;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ClientQueryDTO {
    
    private String id;
    private String nomClient;
    private String prenomClient;
    private String mailClient;
    private String telClient;
    private ClientAdresseDTO adresse;
    private ClientStatus clientStatus;
    private VehiculeQueryDTO vehicule;
    
}
