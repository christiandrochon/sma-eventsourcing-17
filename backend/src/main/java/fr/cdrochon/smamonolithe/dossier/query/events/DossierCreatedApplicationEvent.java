package fr.cdrochon.smamonolithe.dossier.query.events;

import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierQueryDTO;
import org.springframework.context.ApplicationEvent;

/**
 * Event Spring publié après que le DossierCreatedEvent Axon a été persiste en DB.
 * Permet aux services Command de compléter leur CompletableFuture.
 */
public class DossierCreatedApplicationEvent extends ApplicationEvent {
    private final DossierQueryDTO dossier;

    public DossierCreatedApplicationEvent(Object source, DossierQueryDTO dossier) {
        super(source);
        this.dossier = dossier;
    }

    public DossierQueryDTO getDossier() {
        return dossier;
    }
}

