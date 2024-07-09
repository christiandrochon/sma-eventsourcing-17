package fr.cdrochon.thymeleaffrontend.dtos.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdresseClientDTO {
    @NotBlank(message = "champ obligatoire")
    private String numeroDeRue;
    @NotBlank(message = "champ obligatoire")
    private String rue;
    private String complementAdresse;
    @Pattern(regexp = "^[0-9]{5}$", message = "code postal invalide")
    private String cp;
    @NotBlank(message = "champ obligatoire")
    private String ville;
    @NotBlank(message = "champ obligatoire")
    private PaysDTO pays;
}
