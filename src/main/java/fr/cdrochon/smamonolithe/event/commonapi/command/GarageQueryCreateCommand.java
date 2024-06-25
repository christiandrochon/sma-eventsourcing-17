package fr.cdrochon.smamonolithe.event.commonapi.command;

import fr.cdrochon.smamonolithe.event.commonapi.enums.GarageStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

/**
 * Classe immutable. Un event est immutable = une fois qu'iul est créé, on ne peut plus le modifier
 * <p>
 * Implemente la creation d'un client (regle metier)
 */
//public record GarageQueryCreateCommand(@TargetAggregateIdentifier String id, String nomClient, String mailResp) {
public class GarageQueryCreateCommand extends BaseCommand<String> {
    
    @Getter
    private String nomClient;
    @Getter
    private String mailResponsable;
    @Getter
    private GarageStatus garageStatus;
    @Getter
    private Instant dateQuery;
    
    public GarageQueryCreateCommand(String id) {
        super(id);
    }
    
    public GarageQueryCreateCommand(String id, String nomClient, String mailResponsable, GarageStatus garageStatus, Instant dateQuery) {
        super(id);
        this.nomClient = nomClient;
        this.mailResponsable = mailResponsable;
        this.garageStatus = GarageStatus.CREATED;
        this.dateQuery = dateQuery;
    }
    
    public GarageQueryCreateCommand(String id, String nomClient, String mailResponsable) {
        super(id);
        this.nomClient = nomClient;
        this.mailResponsable = mailResponsable;
    }
    
    /**
     * Capture n'importe quelle exception en interne
     *
     * @param exception
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
