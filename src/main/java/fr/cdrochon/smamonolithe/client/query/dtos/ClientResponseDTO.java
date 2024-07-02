package fr.cdrochon.smamonolithe.client.query.dtos;

import fr.cdrochon.smamonolithe.garage.command.enums.GarageStatus;
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
    private ClientAdresseDTO adresse;
    private GarageStatus garageStatus;
}
