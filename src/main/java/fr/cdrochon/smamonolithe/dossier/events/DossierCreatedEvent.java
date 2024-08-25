package fr.cdrochon.smamonolithe.dossier.events;

import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.dossier.query.entities.DossierStatus;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import lombok.Getter;

import java.time.Instant;

@Getter
public class DossierCreatedEvent extends DossierBaseEvent<String> {
    
    
    private final String nomDossier;
    private final Instant dateCreationDossier;
    private final Instant dateModificationDossier;
    private final Client client;
    private final Vehicule vehicule;
    private final DossierStatus dossierStatus;
    private final String clientId;
    private final String vehiculeId;
    
    /**
     * Ajout des id de vehicule et de client
     *
     * @param id                      id du dossier
     * @param nomDossier              nom du dossier
     * @param dateCreationDossier     date de création du dossier
     * @param dateModificationDossier date de modification du dossier
     * @param client                  client du dossier
     * @param vehicule                vehicule du dossier
     * @param dossierStatus           status du dossier
     * @param clientId                id du client
     * @param vehiculeId              id du vehicule
     */
    public DossierCreatedEvent(String id, String nomDossier, Instant dateCreationDossier, Instant dateModificationDossier, Client client, Vehicule vehicule,
                               DossierStatus dossierStatus, String clientId, String vehiculeId) {
        super(id);
        this.nomDossier = nomDossier;
        this.dateCreationDossier = dateCreationDossier;
        this.dateModificationDossier = dateModificationDossier;
        this.client = client;
        this.vehicule = vehicule;
        this.dossierStatus = dossierStatus;
        //TODO verifier si ces ids sont utiles !
        this.clientId = clientId;
        this.vehiculeId = vehiculeId;
    }
}
