package fr.cdrochon.thymeleaffrontend.ui.forms;


import fr.cdrochon.thymeleaffrontend.entity.Adresse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Modèle du formulaire pour CreateCustomer.
 *
 * Note : on peut envisager d'utiliser directement le DTO pour ça.
 * On a toujours ce fichu dilemne :
 *
 * <ul>
 * <li>respecter scrupuleusement les couches et dupliquer éventuellement des
 * données ;
 * <li>réutiliser du code mais être trop laxiste quant à la séparation des
 * couches.
 * </ul>
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateGarageForm {
    
//    @NotNull(message = "champ obligatoire")
//    @Size(min = 3, max = 32)
//    private String id;
    
    @NotBlank(message = "champ obligatoire")
//    @Size(max = 20)
    private String nomGarage;

    @NotBlank(message = "champ obligatoire")
    @Size(min = 3, max = 32)
    private String mailResponsable;

    private Adresse adresse;



   
//    public CustomerCreationDTO toDTO() {
//        String token = getRandomToken().isEmpty() ? null : getRandomToken();
//        return new CustomerCreationDTO(username, password1, getFirstname(), getLastname(), getTelephone(), getEmail(),
//                buildAddressDTO(), getAccountName(), token);
//    }

}
