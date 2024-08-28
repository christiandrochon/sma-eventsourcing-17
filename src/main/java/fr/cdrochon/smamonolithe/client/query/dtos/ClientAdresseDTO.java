package fr.cdrochon.smamonolithe.client.query.dtos;

import com.fasterxml.jackson.annotation.JsonView;
import fr.cdrochon.smamonolithe.client.query.entities.Pays;
import fr.cdrochon.smamonolithe.json.Views;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
/**
 * DTO pour l'adresse d'un client.
 * <p></p>
 * Les annotations JsonView permettent de définir les vues dans lesquelles les attributs de l'objet seront sérialisés. Grace à l'heritage dans la classe
 * Views, tous les attributs ci-dessous seront sérialisés dans la vue VehiculeView, et donc dans la vue ClientView.
 */
public class ClientAdresseDTO {
    
    @JsonView(Views.VehiculeView.class)
    private String numeroDeRue;
    @JsonView(Views.VehiculeView.class)
    private String rue;
    @JsonView(Views.VehiculeView.class)
    private String complementAdresse;
    @JsonView(Views.VehiculeView.class)
    private String cp;
    @JsonView(Views.VehiculeView.class)
    private String ville;
    @JsonView(Views.VehiculeView.class)
    // Enum -> immutable, pas besoin de le convertir en DTO
    private Pays pays;
}
