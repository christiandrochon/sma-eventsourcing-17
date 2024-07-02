package fr.cdrochon.thymeleaffrontend.dtos.client;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientGetDTO {
    private String id;
    private String nomClient;
    private String prenomClient;
    private String mailClient;
    private String telClient;
    private AdresseClientDTO adresse;
}
