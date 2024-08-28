package fr.cdrochon.smamonolithe.client.query.dtos;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import fr.cdrochon.smamonolithe.client.query.entities.ClientStatus;
import fr.cdrochon.smamonolithe.json.Views;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
//evite les boucles infinies dans le json dues aux relations bidirectionnelles
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
/**
 * DTO pour la requête d'un client.
 * <p></p>
 * Les annotations JsonView permettent de définir les vues dans lesquelles les attributs de l'objet seront sérialisés.
 */
public class ClientQueryDTO {
    
    @JsonView(Views.VehiculeView.class)
    private String id;
    @JsonView(Views.VehiculeView.class)
    private String nomClient;
    @JsonView(Views.VehiculeView.class)
    private String prenomClient;
    @JsonView(Views.VehiculeView.class)
    private String mailClient;
    @JsonView(Views.VehiculeView.class)
    private String telClient;
    @JsonView(Views.VehiculeView.class)
    private ClientAdresseDTO adresse;
    @JsonView(Views.VehiculeView.class)
    private ClientStatus clientStatus;
    @JsonView(Views.ClientView.class)
    private VehiculeQueryDTO vehicule;
    
}
