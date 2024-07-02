package fr.cdrochon.thymeleaffrontend.dtos.client;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdresseClientDTO {
    private String numeroDeRue;
    private String rue;
    private String cp;
    private String ville;
}
