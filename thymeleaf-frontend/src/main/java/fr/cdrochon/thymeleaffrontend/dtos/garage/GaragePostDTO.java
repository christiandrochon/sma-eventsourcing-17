package fr.cdrochon.thymeleaffrontend.dtos.garage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GaragePostDTO{
    private String id;
    @NotBlank(message = "champ obligatoire")
    @Size(max = 50)
    private String nomGarage;
    @NotBlank(message = "champ obligatoire")
    @Email(message = "adresse mail invalide")
    private String mailResp;
    @Valid
    private GarageAdresseDTO adresse;
}
