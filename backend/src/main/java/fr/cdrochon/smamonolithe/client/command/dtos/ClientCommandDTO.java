package fr.cdrochon.smamonolithe.client.command.dtos;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.entities.ClientStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Permet de faire le lien entre les services command de l'appli et le monde exteieur
 * <p>
 * Les noms des attributs doivent correspondre à ceux du dto
 */

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Schema(description = "DTO utilisé pour les commandes liées aux clients")
public class ClientCommandDTO {
    
    @Schema(
            description = "Identifiant unique du client",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String id;
    @Schema(
            description = "Nom du client",
            example = "Dupont"
    )
    private String nomClient;
    @Schema(
            description = "Prénom du client",
            example = "Jean"
    )
    private String prenomClient;
    @Schema(
            description = "Adresse mail du client",
            example = "jean.dupont@gmail.com"
    )
    private String mailClient;
    @Schema(
            description = "Téléphone du client",
            example = "+33612345678"
    )
    private String telClient;
    @Schema(
            description = "Adresse complète du client"
    )
    private ClientAdresseDTO adresse;
    
    // Un enum est immutable, pas besoin de le convertir en DTO
    @Schema(
            description = "Statut du client",
            example = "ACTIVE"
    )
    private ClientStatus clientStatus;
    
    //TODO : copie de l'objet AdresseClient pour éviter l'exposition de la représentation interne
    
    /**
     * Copie de l'objet AdresseClient pour éviter l'exposition de la représentation interne
     *
     * @param adresseClient AdresseClient
     */
    public ClientCommandDTO(ClientAdresseDTO adresseClient) {
        this.adresse = adresseClient;
    }
}
