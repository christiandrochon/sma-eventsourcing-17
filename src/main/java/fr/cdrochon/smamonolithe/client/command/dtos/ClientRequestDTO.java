package fr.cdrochon.smamonolithe.client.command.dtos;

import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * permet de faire le lien entre les services command de l'appli et le monde exteieur
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ClientRequestDTO {
    private String id;
    private String nomClient;
    private String prenomClient;
    private String mailClient;
    private String telClient;
    private AdresseClient adresseClient;
}
