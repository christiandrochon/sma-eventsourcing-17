package fr.cdrochon.thymeleaffrontend.dtos.garage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "Champ obligatoire, veuillez renseigner le nom du garage")
    @Size(min = 3, message = "Le nom du garage doit contenir au moins 3 caractères")
    private String nomGarage;
    @NotBlank(message = "Champ obligatoire, veuillez renseigner le mail du responsable")
//    @Email(message = "Adresse mail invalide")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Adresse mail invalide")
    private String mailResp;
    @Valid
    private GarageAdresseDTO adresse;
    
    /**
     * Copie de l'objet AdresseClient pour éviter l'exposition de la représentation interne
     * @return GarageAdresseDTO
     */
//    public @Valid GarageAdresseDTO getAdresse() {
//        return new GarageAdresseDTO(
//            this.adresse.getNumeroDeRue(),
//            this.adresse.getRue(),
//            this.adresse.getCp(),
//            this.adresse.getVille()
//        );
//    }
}
