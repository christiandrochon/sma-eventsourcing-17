package fr.cdrochon.smamonolithe.garage.command.commands;

import fr.cdrochon.smamonolithe.garage.query.dto.GarageAdresseDTO;
import fr.cdrochon.smamonolithe.garage.query.entities.AdresseGarage;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Classe immutable.
 * <p>
 * Chaque command possède un id
 */
@Getter
public class GarageCreateCommand extends GarageBaseCommand<String> {
    
    private final String nomGarage;
    private final String mailResp;
    private final AdresseGarage adresse;
    
    
    /**
     * Copie de l'adresse du garage pour eviter l'exposition de la representation interne
     *
     * @param id        id du garage
     * @param nomGarage nom du garage
     * @param mailResp  mail du responsable
     * @param adresse   adresse du garage
     */
    public GarageCreateCommand(String id, String nomGarage, String mailResp, GarageAdresseDTO adresse) {
        super(id);
        this.nomGarage = nomGarage;
        this.mailResp = mailResp;
        this.adresse = new AdresseGarage(adresse);
    }
    
    /**
     * Capture n'importe quelle exception en interne et affiche son message
     *
     * @param exception Exception
     * @return ResponseEntity<String>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
