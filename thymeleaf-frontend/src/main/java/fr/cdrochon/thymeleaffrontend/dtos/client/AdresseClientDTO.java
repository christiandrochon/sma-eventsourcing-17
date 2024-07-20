package fr.cdrochon.thymeleaffrontend.dtos.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdresseClientDTO {
    @NotBlank(message = "Ce champ est obligatoire")
    private String numeroDeRue;
    @NotBlank(message = "Ce champ est obligatoire")
    private String rue;
    private String complementAdresse;
    @Pattern(regexp = "^[0-9]{5}$", message = "Code postal invalide")
    private String cp;
    @NotBlank(message = "Ce champ est obligatoire")
    private String ville;
    @NotNull(message = "Ce champ est obligatoire")
    private PaysDTO pays;
}
