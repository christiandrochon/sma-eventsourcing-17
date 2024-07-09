package fr.cdrochon.thymeleaffrontend.dtos.dossier;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculePostDTO;
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
    @NotBlank(message = "Le nom du document est obligatoire")
    @Size(min = 3, max = 50)
    private String nomDossier;
//    @NotBlank(message = "La date de création du dossier est obligatoire")
    private String dateCreationDossier;
//    @NotBlank(message = "La date de modification du document est obligatoire")
    private String dateModificationDossier;
    @NotNull(message = "Le client est obligatoire")
    private ClientPostDTO client;
    @NotNull(message = "Le vehicule est obligatoire")
    private VehiculePostDTO vehicule;
    @NotNull(message = "Le status du dossier est obligatoire")
    private DossierStatusDTO dossierStatus;
}
