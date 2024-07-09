package fr.cdrochon.thymeleaffrontend.dtos.dossier;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.cdrochon.thymeleaffrontend.dtos.client.ClientPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeDateConvertDTO;
import lombok.*;

import java.time.Instant;

/**
 * Annotations Json pour le mapping des objets JSON, utiles surtout pour s'assurer d'une bonne deserialisation, mes objets etant semble-t-il imbriqués...
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DossierConvertPostDTO {
    
    private String id;
    @JsonProperty("nomDossier")
    private String nomDossier;
    @JsonProperty("dateCreationDossier")
    private Instant dateCreationDossier;
    @JsonProperty("dateModificationDossier")
    private Instant dateModificationDossier;
    @JsonProperty("client")
    private ClientPostDTO client;
    @JsonProperty("vehicule")
    private VehiculeDateConvertDTO vehicule;
    @JsonProperty("dossierStatus")
    private DossierStatusDTO dossierStatus;
}
