package fr.cdrochon.thymeleaffrontend.dtos.client;

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
//    private AdresseClientDTO adresse;
    
    @NotBlank(message = "champ obligatoire")
    private String numeroDeRue;
    @NotBlank(message = "champ obligatoire")
    private String rue;
    private String complementAdresse;
    @Pattern(regexp = "^[0-9]{5}$", message = "code postal invalide")
    private String cp;
    @NotBlank(message = "champ obligatoire")
    private String ville;
//    @NotBlank(message = "champ obligatoire")
    private PaysDTO pays;
    
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
