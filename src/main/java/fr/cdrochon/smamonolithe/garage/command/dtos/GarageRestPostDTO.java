package fr.cdrochon.smamonolithe.garage.command.dtos;

import fr.cdrochon.smamonolithe.garage.query.dto.GarageAdresseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création d'un garage
 * Le nom des attributs est identique à ceux de l'entité Garage pour faciliter la conversion entre les deux.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GarageRestPostDTO {
//    private String id;
    private String nomGarage;
    private String mailResp;
    private GarageAdresseDTO adresse;
    //    private GarageStatus garageStatus;
    //    private Instant dateQuery;
    
    /**
     * Copie de l'objet AdresseGarage pour éviter l'exposition de la représentation interne
     * @param adresseGarage AdresseGarage
     */
    public GarageRestPostDTO(GarageAdresseDTO adresseGarage) {
        this.adresse = adresseGarage;
    }
}
