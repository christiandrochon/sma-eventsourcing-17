package fr.cdrochon.smamonolithe.client.query.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClientAdresseDTO {
    
    private String numeroDeRue;
    private String rue;
    private String cp;
    private String ville;
}
