package fr.cdrochon.thymeleaffrontend.dtos.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientPostDTO {
    
    private String id;
    @NotBlank(message = "Le nom du client est obligatoire, merci de le renseigner.")
    @Size(min = 2, message = "Le nom du client doit contenir au moins 2 caractères.")
    private String nomClient;
    @NotBlank(message = "Le prénom du client est obligatoire, merci de le renseigner.")
    @Size(min = 2, message = "Le prénom du client doit contenir au moins 2 caractères.")
    private String prenomClient;
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide, vérifiez le format de l'adresse mail.")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Adresse mail invalide. Vérifiez le format de l'adresse mail.")
    private String mailClient;
    @NotBlank(message = "Le téléphone est obligatoire, merci de le renseigner.")
    @Pattern(regexp = "^\\d{10}$", message = "Le numéro de téléphone doit contenir 10 chiffres. Exemple : 0601020304")
    private String telClient;
    @Valid
    private AdresseClientDTO adresse;
    @NotNull(message = "Le statut du client est obligatoire, merci de le renseigner.")
    private ClientStatusDTO clientStatus;
    
    /**
     * Copie de l'objet AdresseClient pour éviter l'exposition de la représentation interne
     * @return AdresseClientDTO
     */
//    public @Valid AdresseClientDTO getAdresse() {
//        return new AdresseClientDTO(
//            this.adresse.getNumeroDeRue(),
//            this.adresse.getRue(),
//            this.adresse.getCp(),
//            this.adresse.getVille()
//        );
//    }
}
