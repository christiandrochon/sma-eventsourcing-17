package fr.cdrochon.smamonolithe.dossier.command.dtos;

import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import fr.cdrochon.smamonolithe.dossier.query.entities.DossierStatus;
import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
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
public class DossierCommandDTO {

    private String id;
    private String nomDossier;
    private Instant dateCreationDossier;
    private Instant dateModificationDossier;
    private ClientCommandDTO client;
    private VehiculeCommandDTO vehicule;
    // Un enum est immutable, pas besoin de le convertir en DTO
    private DossierStatus dossierStatus;
}
