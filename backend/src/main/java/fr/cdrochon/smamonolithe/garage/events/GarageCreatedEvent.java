package fr.cdrochon.smamonolithe.garage.events;

import fr.cdrochon.smamonolithe.garage.command.enums.GarageStatus;
import fr.cdrochon.smamonolithe.garage.query.entities.AdresseGarage;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Evenement de creation d'un garage (suite à une commande).
 * <p>
 * Objet immutable
 */
@Getter
@NoArgsConstructor
public class GarageCreatedEvent extends GarageBaseEvent<String> {
    
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
