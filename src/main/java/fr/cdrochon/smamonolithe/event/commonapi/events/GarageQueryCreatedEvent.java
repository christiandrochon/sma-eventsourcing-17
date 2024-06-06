package fr.cdrochon.smamonolithe.event.commonapi.events;

import fr.cdrochon.smamonolithe.event.commonapi.command.BaseCommand;
import fr.cdrochon.smamonolithe.event.commonapi.command.GarageQueryCreateCommand;
import fr.cdrochon.smamonolithe.event.commonapi.enums.GarageStatus;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Les events sont exprimés dans le passé (pour le nommage)
 */
//@SuperBuilder
//@Getter
//public record GarageQueryCreatedEvent(String id, String nomClient, String mailResp, GarageStatus garageStatus) {
public class GarageQueryCreatedEvent extends BaseEvent<String> {
    
    // je ne garde que le nom du client
    @Getter
    private String nomGarage;
    @Getter
    private String mailResponsable;
    @Getter
    private GarageStatus clientStatus;
    @Getter
    private Instant dateQuery;
    
    public GarageQueryCreatedEvent(String id, String nomClient, String mailResponsable, GarageStatus clientStatus, Instant dateQuery) {
        super(id);
        this.nomGarage = nomClient;
        this.mailResponsable = mailResponsable;
        this.clientStatus = clientStatus;
        this.dateQuery = dateQuery;
    }
    
//    public static GarageQueryCreateCommand createCommand(GarageQueryCreateCommand garageQueryCreateCommand){
//        return BaseCommand.BaseCommandBuilder
//    }
}
