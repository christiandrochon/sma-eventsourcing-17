package fr.cdrochon.thymeleaffrontend.dtos.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(min = 2, message = "Le nom de la rue doit contenir au moins 2 caractères.")
    private String rue;
    private String complementAdresse;
    @Pattern(regexp = "^[0-9]{5}$", message = "Code postal invalide")
    private String cp;
    @Size(min = 2, message = "Le nom de la ville doit contenir au moins 2 caractères.")
    @NotBlank(message = "Ce champ est obligatoire")
    private String ville;
    private PaysDTO pays;
}
