package fr.cdrochon.smamonolithe.garage.commonapi.events;

import fr.cdrochon.smamonolithe.garage.commonapi.enums.GarageStatus;
import fr.cdrochon.smamonolithe.garage.query.entities.AdresseGarage;
import lombok.Getter;

/**
 * Les events sont exprimés dans le passé (pour le nommage).
 * Chaque event possede un id.
 *
 * Objet immutable
 */

public class GarageCreatedEvent extends BaseEvent<String> {
    
    // je ne garde que le nom du client
    @Getter private final String nomGarage;
    @Getter private final String mailResponsable;
    @Getter private final AdresseGarage adresseGarage;
    @Getter private final GarageStatus clientStatus;
//    @Getter
//    private Instant dateQuery;
    
    public GarageCreatedEvent(String id, String nomGarage, String mailResponsable, AdresseGarage adresseGarage, GarageStatus garageStatus) {
        super(id);
        this.nomGarage = nomGarage;
        this.mailResponsable = mailResponsable;
        this.adresseGarage = adresseGarage;
        this.clientStatus = garageStatus;
//        this.dateQuery = dateQuery;
    }

}
