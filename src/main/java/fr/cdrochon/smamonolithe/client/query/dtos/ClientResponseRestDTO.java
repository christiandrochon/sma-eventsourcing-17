package fr.cdrochon.smamonolithe.client.query.dtos;

import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import fr.cdrochon.smamonolithe.garage.command.enums.GarageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientResponseRestDTO {
    
    private String id;
    private String nomClient;
    private String prenomClient;
    private String mailClient;
    private String telClient;
    private AdresseClient adresse;
    private GarageStatus garageStatus;
}
