package fr.cdrochon.smamonolithe.garage.command.dtos;

import fr.cdrochon.smamonolithe.garage.query.dto.GarageAdresseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création d'un garage.
 * <p>
 * Le nom des attributs est identique à ceux de l'entité Garage pour faciliter la conversion entre les deux. Il n'y a pas de status à ce stade, il sera géré par les events
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GarageCommandDTO {
    
    //id necessaire pour afficher les details d'un garage à l'user après sa creation
    private String id;
    private String nomGarage;
    private String mailResp;
    private GarageAdresseDTO adresse;
    //le statut n'est utile que pour l'agrégat, pas besoin de le passer à un user
    
    /**
     * Copie de l'objet AdresseGarage pour éviter l'exposition de la représentation interne
     *
     * @param adresseGarage AdresseGarage
     */
    public GarageCommandDTO(GarageAdresseDTO adresseGarage) {
        this.adresse = adresseGarage;
    }
}
