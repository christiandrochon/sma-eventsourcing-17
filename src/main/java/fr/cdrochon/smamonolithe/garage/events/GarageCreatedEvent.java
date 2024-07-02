package fr.cdrochon.smamonolithe.garage.events;

import fr.cdrochon.smamonolithe.garage.command.enums.GarageStatus;
import fr.cdrochon.smamonolithe.garage.query.entities.AdresseGarage;
import lombok.Getter;

/**
 * Les events sont exprimés dans le passé (pour le nommage).
 * Chaque event possede un id.
 *
 * Objet immutable
 */
@Getter
public class GarageCreatedEvent extends GarageBaseEvent<String> {
    
    // je ne garde que le nom du client
    private final String nomGarage;
    private final String mailResponsable;
    private final AdresseGarage adresseGarage;
    private final GarageStatus clientStatus;

    
    public GarageCreatedEvent(String id, String nomGarage, String mailResponsable, AdresseGarage adresseGarage, GarageStatus garageStatus) {
        super(id);
        this.nomGarage = nomGarage;
        this.mailResponsable = mailResponsable;
        this.adresseGarage = adresseGarage;
        this.clientStatus = garageStatus;
    }

}
