package fr.cdrochon.thymeleaffrontend.dtos.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientThymDTO {
    
    private String id;

    @Size(min = 2, message = "Le nom du client doit contenir au moins 2 caractères.")
    private String nomClient;
    @Size(min = 2, message = "Le prénom du client doit contenir au moins 2 caractères.")
    private String prenomClient;
    @Email(message = "Email invalide, vérifiez le format de l'adresse mail.")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Adresse mail invalide. Vérifiez le format de l'adresse mail.")
    private String mailClient;
    @Pattern(regexp = "^\\d{10}$", message = "Le numéro de téléphone doit contenir 10 chiffres. Exemple : 0601020304")
    private String telClient;
    @Valid
    private AdresseClientDTO adresse;
    private ClientStatusDTO clientStatus;
    
    //TODO : copie de l'objet AdresseClient pour éviter l'exposition de la représentation interne ?
//    /**
//     * Copie de l'objet AdresseClient pour éviter l'exposition de la représentation interne
//     * @return AdresseClientDTO
//     */
//    public @Valid AdresseClientDTO getAdresse() {
//        return new AdresseClientDTO(
//            this.adresse.getNumeroDeRue(),
//            this.adresse.getRue(),
//            this.adresse.getCp(),
//            this.adresse.getVille()
//        );
//    }
}
