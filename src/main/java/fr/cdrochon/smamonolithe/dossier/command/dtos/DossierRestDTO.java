package fr.cdrochon.smamonolithe.dossier.command.dtos;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.dossier.query.entities.DossierStatus;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import lombok.*;

import java.time.Instant;

/**
 * Permet de faire le lien entre les services command de l'appli et le monde exteieur
 *
 * Les noms des attributs doivent correspondre à ceux du dto
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DossierRestDTO {

    private String id;
    private String nomDossier;
    private Instant dateCreationDossier;
    private Instant dateModificationDossier;
    private Client client;
    private Vehicule vehicule;
    private DossierStatus dossierStatus;
}
