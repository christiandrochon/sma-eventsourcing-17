package fr.cdrochon.smamonolithe.client.query.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ClientResponseDTO {
    private String id;
    private String nomClient;
    private String prenomClient;
    private String mailClient;
    private String telClient;
//    private AdresseClient adresse;
    private ClientAdresseDTO adresse;
    private ClientStatusDTO clientStatus;
}
