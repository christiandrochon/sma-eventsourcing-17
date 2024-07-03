package fr.cdrochon.thymeleaffrontend.ui.validation;

import fr.cdrochon.thymeleaffrontend.entity.client.AdresseClient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateClientForm {
    
    @NotBlank(message = "champ obligatoire")
    @Size(min = 3, max = 50)
    private String nomClient;
    @NotBlank(message = "champ obligatoire")
    @Size(min = 3, max = 50)
    private String prenomClient;
    @NotBlank(message = "champ obligatoire")
    @Size(min = 10, max = 10)
    private String telephoneClient;
    @NotBlank(message = "champ obligatoire")
    @Email
    private Email mailClient;

    private AdresseClient adresse;
}
