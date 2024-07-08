package fr.cdrochon.thymeleaffrontend.dtos.client;

import fr.cdrochon.thymeleaffrontend.entity.client.ClientStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientPostDTO {
    
    private String id;
    @NotBlank(message = "champ obligatoire")
    @Size(min = 3, max = 50)
    private String nomClient;
    @NotBlank(message = "champ obligatoire")
    @Size(min = 3, max = 50)
    private String prenomClient;
    @NotBlank(message = "champ obligatoire")
    @Email(message = "email invalide")
    private String mailClient;
    @NotBlank(message = "champ obligatoire")
    @Size(min = 10, max = 10)
    private String telClient;
    @Valid
    private AdresseClientDTO adresse;
    private ClientStatus clientStatus;
    
    /**
     * Copie de l'objet AdresseClient pour éviter l'exposition de la représentation interne
     * @return AdresseClientDTO
     */
    public @Valid AdresseClientDTO getAdresse() {
        return new AdresseClientDTO(
            this.adresse.getNumeroDeRue(),
            this.adresse.getRue(),
            this.adresse.getCp(),
            this.adresse.getVille()
        );
    }
}
