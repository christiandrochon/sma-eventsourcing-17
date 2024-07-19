package fr.cdrochon.smamonolithe.garage.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import fr.cdrochon.smamonolithe.garage.command.enums.GarageStatus;
import fr.cdrochon.smamonolithe.garage.query.entities.AdresseGarage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Les events sont exprimés dans le passé (pour le nommage).
 * Chaque event possede un id.
 * <p>
 * Objet immutable
 */
@Getter @NoArgsConstructor
public class GarageCreatedEvent extends GarageBaseEvent<String> {
    
    // je ne garde que le nom du client
    private String nomGarage;
    private String mailResponsable;
    private AdresseGarage adresseGarage;
    private GarageStatus garageStatus;

    public GarageCreatedEvent(String id, String nomGarage, String mailResponsable, AdresseGarage adresseGarage, GarageStatus garageStatus) {
        super(id);
        this.nomGarage = nomGarage;
        this.mailResponsable = mailResponsable;
        this.adresseGarage = adresseGarage;
        this.garageStatus = garageStatus;
    }
    
}
