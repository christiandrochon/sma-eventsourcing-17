package fr.cdrochon.thymeleaffrontend.dtos.dossier;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymDTO;
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
public class DossierThymConvertDTO {
    
    private String id;
    private String nomDossier;
    private Instant dateCreationDossier;
    private Instant dateModificationDossier;
    private ClientThymDTO client;
    private VehiculeDateConvertDTO vehicule;
    private DossierStatusThymDTO dossierStatus;
}
