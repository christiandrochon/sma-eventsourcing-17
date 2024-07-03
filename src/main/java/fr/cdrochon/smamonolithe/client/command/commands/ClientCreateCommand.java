package fr.cdrochon.smamonolithe.client.command.commands;

import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Classe abstraite immutable qui effectue une command
 * Implemente l'ajout d'un client via un event sur le CommandBus
 * <p>
 * Chaque command possede un id
 */
@Getter
public class ClientCreateCommand extends ClientBaseCommand<String> {

    private final String nomClient;
    private final String prenomClient;
    private final String mailClient;
    private final String telClient;
    private final AdresseClient adresseClient;
    
    public ClientCreateCommand(String id, String nomClient, String prenomClient, String mailClient, String telClient, AdresseClient adresseClient) {
        super(id);
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.mailClient = mailClient;
        this.telClient = telClient;
        this.adresseClient = adresseClient;
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
