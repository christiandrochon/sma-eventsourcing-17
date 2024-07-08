package fr.cdrochon.thymeleaffrontend.dtos.dossier;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculePostDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DossierConvertPostDTO {
    
    private String id;
    private String nomDossier;
    private Instant dateCreationDossier;
    private Instant dateModificationDossier;
    private ClientPostDTO client;
    private VehiculePostDTO vehicule;
    private DossierStatusDTO dossierStatus;
}
