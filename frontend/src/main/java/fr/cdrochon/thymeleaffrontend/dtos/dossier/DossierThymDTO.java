package fr.cdrochon.thymeleaffrontend.dtos.dossier;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DossierThymDTO {
    
    private String id;
    @NotBlank(message = "Le nom du dossier est obligatoire")
    @Size(min = 3, message = "Le nom du dossier doit contenir au moins 3 caractères.")
    private String nomDossier;
    // inutile car non apparent à l'ecran
    private String dateCreationDossier;
    // inutile car non apparent à l'ecran
    private String dateModificationDossier;
    @Valid
    private ClientThymDTO client;
    @Valid
    private VehiculeThymDTO vehicule;
    private DossierStatusThymDTO dossierStatus;
}
