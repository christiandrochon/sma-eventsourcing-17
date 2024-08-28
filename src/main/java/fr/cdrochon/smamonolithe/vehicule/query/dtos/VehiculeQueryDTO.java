package fr.cdrochon.smamonolithe.vehicule.query.dtos;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.json.Views;
import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO pour la requête d'un véhicule.
 * <p></p>
 * Les annotations JsonView permettent de définir les vues dans lesquelles les attributs de l'objet seront sérialisés. Grace à l'heritage dans la classe Views,
 * tous les attributs de la vue VehiculeView seront sérialisés dans la vue ClientView.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//evite les boucles infinies dans le json dues aux relations bidirectionnelles
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class VehiculeQueryDTO {
    
    @JsonView(Views.VehiculeView.class)
    private String id;
    @JsonView(Views.VehiculeView.class)
    private String immatriculationVehicule;
    @JsonView(Views.VehiculeView.class)
    private Instant dateMiseEnCirculationVehicule;
    @JsonView(Views.VehiculeView.class)
    private VehiculeStatus vehiculeStatus;
    @JsonView(Views.VehiculeView.class)
    private ClientQueryDTO client;
    
}
