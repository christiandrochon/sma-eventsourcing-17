package fr.cdrochon.thymeleaffrontend.dtos.garage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GarageAdresseDTO {
    @NotBlank(message = "Champ obligatoire")
    private String numeroDeRue;
    @NotBlank(message = "Champ obligatoire")
    private String rue;
    @Pattern(regexp = "^[0-9]{5}$", message = "Code postal invalide")
    private String cp;
    @NotBlank(message = "Champ obligatoire")
    private String ville;
}
