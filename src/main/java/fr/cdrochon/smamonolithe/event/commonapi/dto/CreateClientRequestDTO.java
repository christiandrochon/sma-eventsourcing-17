package fr.cdrochon.smamonolithe.event.commonapi.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateClientRequestDTO {
    private String id;
    private String nomClient;
    private String prenomClient;
}
