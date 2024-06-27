package fr.cdrochon.smamonolithe.garage.query.dto;

import fr.cdrochon.smamonolithe.garage.command.enums.GarageStatus;
import fr.cdrochon.smamonolithe.garage.query.entities.AdresseGarage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GarageResponseRestDTO {
    private String id;
    private String nomGarage;
    private String mailResp;
    private AdresseGarage adresse;
    private GarageStatus garageStatus;
}
