package fr.cdrochon.smamonolithe.garage.command.commands;

import fr.cdrochon.smamonolithe.garage.query.entities.AdresseGarage;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

/**
 * Classe immutable. Un event est immutable = une fois qu'il est créé, on ne peut plus le modifier
 * <p>
 * Implemente la creation d'un client (regle metier)
 * Chaque command possede un id
 */
@Getter
public class GarageCreateCommand extends GarageBaseCommand<String> {
    
    private String nomGarage;
    private String mailResponsable;
    private AdresseGarage adresseGarage;
    private Instant dateQuery;
    
    public GarageCreateCommand(String id) {
        super(id);
    }
    
    public GarageCreateCommand(String id, String nomGarage, String mailResponsable, Instant dateQuery) {
        super(id);
        this.nomGarage = nomGarage;
        this.mailResponsable = mailResponsable;
        this.dateQuery = dateQuery;
    }
    
    public GarageCreateCommand(String id, String nomGarage, String mailResponsable, AdresseGarage adresseGarage) {
        super(id);
        this.nomGarage = nomGarage;
        this.mailResponsable = mailResponsable;
        this.adresseGarage = adresseGarage;
    }
    
    /**
     * Capture n'importe quelle exception en interne et affiche son message
     *
     * @param exception
     * @return ResponseEntity<String>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
