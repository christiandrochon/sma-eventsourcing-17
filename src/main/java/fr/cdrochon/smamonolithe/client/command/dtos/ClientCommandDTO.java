package fr.cdrochon.smamonolithe.client.command.dtos;

import fr.cdrochon.smamonolithe.client.query.entities.ClientStatus;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import lombok.*;
/**
 * Permet de faire le lien entre les services command de l'appli et le monde exteieur
 *<p>
 * Les noms des attributs doivent correspondre à ceux du dto
 */

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ClientCommandDTO {
    
    private String id;
    private String nomClient;
    private String prenomClient;
    private String mailClient;
    private String telClient;
    private ClientAdresseDTO adresse;
    // Un enum est immutable, pas besoin de le convertir en DTO
    private ClientStatus clientStatus;
    
    //TODO : copie de l'objet AdresseClient pour éviter l'exposition de la représentation interne
    /**
     * Copie de l'objet AdresseClient pour éviter l'exposition de la représentation interne
     * @param adresseClient AdresseClient
     */
    public ClientCommandDTO(ClientAdresseDTO adresseClient) {
        this.adresse = adresseClient;
    }

}
