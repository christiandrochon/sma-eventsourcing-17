package fr.cdrochon.smamonolithe.client.command.dtos;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import lombok.*;
/**
 * Permet de faire le lien entre les services command de l'appli et le monde exteieur
 */

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ClientRestPostDTO {
//    private String id;
    private String nomClient;
    private String prenomClient;
    private String mailClient;
    private String telClient;
    private ClientAdresseDTO adresseClient;
    
    /**
     * Copie de l'objet AdresseClient pour éviter l'exposition de la représentation interne
     * @param adresseClient AdresseClient
     */
    public ClientRestPostDTO(ClientAdresseDTO adresseClient) {
        this.adresseClient = adresseClient;
    }
}
