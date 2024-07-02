package fr.cdrochon.thymeleaffrontend.dtos.client;

import fr.cdrochon.thymeleaffrontend.entity.AdresseClient;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientPostDTO {
    private String id;
    private String nomClient;
    private String prenomClient;
    private String mailClient;
    private String telClient;
    //FIXME : utiliser l'adresse DTO ?
    private AdresseClient adresse;
}
