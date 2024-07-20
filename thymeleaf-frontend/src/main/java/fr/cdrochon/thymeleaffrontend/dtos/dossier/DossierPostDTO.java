package fr.cdrochon.thymeleaffrontend.dtos.dossier;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculePostDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DossierPostDTO {
    
    private String id;
    @NotBlank(message = "Le nom du dossier est obligatoire, veuillez le renseigner.")
    @Size(min = 3, message = "Le nom du dossier doit contenir au moins 3 caractères.")
    private String nomDossier;
    private String dateCreationDossier;
    private String dateModificationDossier;
    @Valid
    private ClientPostDTO client;
    @Valid
    private VehiculePostDTO vehicule;
    @NotNull(message = "Le status du dossier est obligatoire, merci de le renseigner.")
    private DossierStatusDTO dossierStatus;
}
