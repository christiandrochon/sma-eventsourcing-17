package fr.cdrochon.smamonolithe.garage.query.events;

import fr.cdrochon.smamonolithe.garage.query.dto.GarageQueryDTO;
import org.springframework.context.ApplicationEvent;

/**
 * Event Spring publié après que le GarageCreatedEvent Axon a été persiste en DB.
 * Permet aux services Command de compléter leur CompletableFuture.
 */
public class GarageCreatedApplicationEvent extends ApplicationEvent {
    private final GarageQueryDTO garage;

    public GarageCreatedApplicationEvent(Object source, GarageQueryDTO garage) {
        super(source);
        this.garage = garage;
    }

    public GarageQueryDTO getGarage() {
        return garage;
    }
}


