package fr.cdrochon.thymeleaffrontend.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdresseGarageDTO {
    private String numeroDeRue;
    private String rue;
    private String cp;
    private String ville;
}
