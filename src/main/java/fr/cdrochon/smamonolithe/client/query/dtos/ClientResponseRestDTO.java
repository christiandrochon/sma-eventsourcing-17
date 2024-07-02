package fr.cdrochon.smamonolithe.client.query.dtos;

import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import fr.cdrochon.smamonolithe.garage.command.enums.GarageStatus;
import fr.cdrochon.smamonolithe.garage.query.entities.AdresseGarage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientResponseRestDTO {
    
    private String id;
    private String nomGarage;
    private String mailResp;
    private AdresseClient adresse;
    private GarageStatus garageStatus;
}
