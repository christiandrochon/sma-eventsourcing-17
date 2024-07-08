package fr.cdrochon.smamonolithe.dossier.command.commands;

import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.dossier.query.entities.DossierStatus;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

/**
 * Classe abstraite immutable qui effectue une command
 * Implemente l'ajout d'un dossier via un event sur le CommandBus
 * <p>
 * Chaque command possede un id
 */
@Getter
public class DossierCreateCommand extends DossierBaseCommand<String> {
    
    private final String nomDossier;
    private final Instant dateCreationDossier;
    private final Instant dateModificationDossier;
    private final Client client;
    private final Vehicule vehicule;
    private final DossierStatus dossierStatus;
    
    public DossierCreateCommand(String id, String nomDossier, Instant dateCreationDossier, Instant dateModificationDossier, Client client, Vehicule vehicule,
                                DossierStatus dossierStatus) {
        super(id);
        this.nomDossier = nomDossier;
        this.dateCreationDossier = dateCreationDossier;
        this.dateModificationDossier = dateModificationDossier;
        this.client = client;
        this.vehicule = vehicule;
        this.dossierStatus = dossierStatus;
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
