package fr.cdrochon.smamonolithe.garage.command.dtos;

import fr.cdrochon.smamonolithe.garage.query.dto.GarageAdresseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DTO utilisé pour les commandes liées aux garages")
public class GarageCommandDTO {
    
    //id necessaire pour afficher les details d'un garage à l'user après sa creation
    @Schema(description = "Identifiant unique du garage", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    @Schema(description = "Nom du garage", example = "Garage Central Paris")
    private String nomGarage;
    @Schema(description = "Email du responsable du garage", example = "contact@garage-central.com")
    private String mailResp;
    @Schema(description = "Adresse du garage")
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
