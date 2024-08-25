package fr.cdrochon.smamonolithe.dossier.query.dtos;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.dossier.query.entities.DossierStatus;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DossierQueryDTO {
    private String id;
    private String nomDossier;
    private Instant dateCreationDossier;
    private Instant dateModificationDossier;
    private ClientQueryDTO client;
    private VehiculeQueryDTO vehicule;
    //enum is immutable, no need to convert it to DTO
    private DossierStatus dossierStatus;
}
