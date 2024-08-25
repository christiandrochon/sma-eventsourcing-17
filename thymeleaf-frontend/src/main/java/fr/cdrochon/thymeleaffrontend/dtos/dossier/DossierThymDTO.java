package fr.cdrochon.thymeleaffrontend.dtos.dossier;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DossierThymDTO {
    
    private String id;

    @Size(min = 3, message = "Le nom du dossier doit contenir au moins 3 caractères.")
    private String nomDossier;
    private String dateCreationDossier;
    private String dateModificationDossier;
    @Valid
    private ClientThymDTO client;
    @Valid
    private VehiculeThymDTO vehicule;
    private DossierStatusThymDTO dossierStatus;
}
