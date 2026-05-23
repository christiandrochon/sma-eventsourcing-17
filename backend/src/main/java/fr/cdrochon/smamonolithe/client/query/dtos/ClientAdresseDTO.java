package fr.cdrochon.smamonolithe.client.query.dtos;

import com.fasterxml.jackson.annotation.JsonView;
import fr.cdrochon.smamonolithe.client.query.entities.Pays;
import fr.cdrochon.smamonolithe.json.Views;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO pour l'adresse d'un client.
 * <p></p>
 * Les annotations JsonView permettent de définir les vues dans lesquelles les attributs de l'objet seront sérialisés. Grace à l'heritage dans la classe Views,
 * tous les attributs ci-dessous seront sérialisés dans la vue VehiculeView, et donc dans la vue ClientView.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Schema(description = "DTO représentant l'adresse d'un client")
public class ClientAdresseDTO {
    
    @JsonView(Views.VehiculeView.class)
    @Schema(description = "Numéro de rue", example = "42")
    private String numeroDeRue;
    @JsonView(Views.VehiculeView.class)
    @Schema(description = "Nom de la rue", example = "rue de la Paix")
    private String rue;
    @JsonView(Views.VehiculeView.class)
    @Schema(description = "Complément d'adresse (Appartement, Bâtiment, etc.)", example = "Appartement 5")
    private String complementAdresse;
    @JsonView(Views.VehiculeView.class)
    @Schema(description = "Code postal", example = "75001")
    private String cp;
    @JsonView(Views.VehiculeView.class)
    @Schema(description = "Ville", example = "Paris")
    private String ville;
    @JsonView(Views.VehiculeView.class)
    @Schema(description = "Pays", example = "FRANCE")
    // Enum -> immutable, pas besoin de le convertir en DTO
    private Pays pays;
}
