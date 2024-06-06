package fr.cdrochon.smamonolithe.event.commonapi.events;

import fr.cdrochon.smamonolithe.event.commonapi.enums.GarageStatus;
import lombok.Getter;

/**
 * Les events sont exprimés dans le passé (pour le nommage)
 */
//public record GarageQueryCreatedEvent(String id, String nomClient, String mailResp, GarageStatus garageStatus) {
    public class GarageQueryCreatedEvent extends BaseEvent<String> {
    
    // je ne garde que le nom du client
    @Getter private String nomGarage;
    @Getter private String mailResponsable;
    @Getter private GarageStatus clientStatus;

    public GarageQueryCreatedEvent(String id, String nomClient, String mailResponsable, GarageStatus clientStatus) {
        super(id);
        this.nomGarage = nomClient;
        this.mailResponsable = mailResponsable;
        this.clientStatus = clientStatus;
    }
}
