package fr.cdrochon.smamonolithe.garage.command.dtos;

import fr.cdrochon.smamonolithe.garage.query.entities.AdresseGarage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GarageRestPostDTO {
//    private String id;
    private String nomGarage;
    private String emailContactGarage;
    private AdresseGarage adresseGarage;
    //    private GarageStatus garageStatus;
    //    private Instant dateQuery;
}
