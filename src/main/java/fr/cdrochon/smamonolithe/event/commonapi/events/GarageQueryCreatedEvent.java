package fr.cdrochon.smamonolithe.event.commonapi.events;

import fr.cdrochon.smamonolithe.event.commonapi.enums.GarageStatus;
import fr.cdrochon.smamonolithe.event.query.entities.GarageQuery;
import lombok.Getter;

/**
 * Les events sont exprimés dans le passé (pour le nommage).
 * Chaque event possede un id.
 *
 * Objet immutable
 */

public class GarageQueryCreatedEvent extends BaseEvent<String> {
    
    // je ne garde que le nom du client
    @Getter private final String nomGarage;
    @Getter private final String mailResponsable;
    @Getter private final GarageStatus clientStatus;
//    @Getter
//    private Instant dateQuery;
    
    public GarageQueryCreatedEvent(String id, String nomGarage, String mailResponsable, GarageStatus garageStatus) {
        super(id);
        this.nomGarage = nomGarage;
        this.mailResponsable = mailResponsable;
        this.clientStatus = garageStatus;
//        this.dateQuery = dateQuery;
    }

}
