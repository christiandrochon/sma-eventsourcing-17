package fr.cdrochon.thymeleaffrontend.dtos.client;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymConvertDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Ce champ est obligatoire")
    @Size(min = 2, message = "Le nom du client doit contenir au moins 2 caractères.")
    private String nomClient;
    @NotBlank(message = "Ce champ est obligatoire")
    @Size(min = 2, message = "Le prénom du client doit contenir au moins 2 caractères.")
    private String prenomClient;
    @NotBlank(message = "Ce champ est obligatoire")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Adresse mail invalide. Vérifiez le format de l'adresse mail.")
    private String mailClient;
    @NotBlank(message = "Ce champ est obligatoire")
    @Pattern(regexp = "^\\d{10}$", message = "Le numéro de téléphone doit contenir 10 chiffres. Exemple : 0601020304")
    private String telClient;
    @Valid
    private AdresseClientDTO adresse;
    private ClientStatusDTO clientStatus;
    @Valid
    private VehiculeThymDTO vehicule;
}
