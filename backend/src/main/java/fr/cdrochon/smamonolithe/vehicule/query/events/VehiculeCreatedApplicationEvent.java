package fr.cdrochon.smamonolithe.vehicule.query.events;

import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import org.springframework.context.ApplicationEvent;

/**
 * Event Spring publié après que le VehiculeCreatedEvent Axon a été persiste en DB.
 * Permet aux services Command de compléter leur CompletableFuture.
 */
public class VehiculeCreatedApplicationEvent extends ApplicationEvent {
    private final VehiculeQueryDTO vehicule;

    public VehiculeCreatedApplicationEvent(Object source, VehiculeQueryDTO vehicule) {
        super(source);
        this.vehicule = vehicule;
    }

    public VehiculeQueryDTO getVehicule() {
        return vehicule;
    }
}


