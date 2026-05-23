package fr.cdrochon.smamonolithe.client.query.dtos;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import fr.cdrochon.smamonolithe.client.query.entities.ClientStatus;
import fr.cdrochon.smamonolithe.json.Views;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO pour la requête d'un client.
 * <p></p>
 * Les annotations JsonView permettent de définir les vues dans lesquelles les attributs de l'objet seront sérialisés.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
//evite les boucles infinies dans le json dues aux relations bidirectionnelles
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Schema(description = "DTO représentant un client dans une requête de lecture")
public class ClientQueryDTO {
    
    @JsonView(Views.VehiculeView.class)
    @Schema(description = "Identifiant unique du client", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    @JsonView(Views.VehiculeView.class)
    @Schema(description = "Nom du client", example = "Dupont")
    private String nomClient;
    @JsonView(Views.VehiculeView.class)
    @Schema(description = "Prénom du client", example = "Jean")
    private String prenomClient;
    @JsonView(Views.VehiculeView.class)
    @Schema(description = "Adresse email du client", example = "jean.dupont@gmail.com")
    private String mailClient;
    @JsonView(Views.VehiculeView.class)
    @Schema(description = "Numéro de téléphone du client", example = "+33612345678")
    private String telClient;
    @JsonView(Views.VehiculeView.class)
    @Schema(description = "Adresse complète du client")
    private ClientAdresseDTO adresse;
    @JsonView(Views.VehiculeView.class)
    @Schema(description = "Statut du client (ACTIVE, INACTIVE, SUSPENDED)", example = "ACTIVE")
    private ClientStatus clientStatus;
    @JsonView(Views.ClientView.class)
    @Schema(description = "Véhicule associé au client")
    private VehiculeQueryDTO vehicule;
    
}
