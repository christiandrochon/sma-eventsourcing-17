package fr.cdrochon.smamonolithe.garage.command.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRequestDTO {
    private String id;
    private String nomClient;
    private String prenomClient;
}
