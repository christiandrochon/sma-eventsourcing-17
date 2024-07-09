package fr.cdrochon.smamonolithe.client.query.dtos;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClientAdresseDTO {
    
    private String numeroDeRue;
    private String rue;
    private String complementAdresse;
    private String cp;
    private String ville;
    private PaysDTO pays;
}
