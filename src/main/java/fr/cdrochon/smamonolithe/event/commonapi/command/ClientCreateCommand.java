package fr.cdrochon.smamonolithe.event.commonapi.command;

import fr.cdrochon.smamonolithe.event.commonapi.dto.CreateClientRequestDTO;
import lombok.Getter;

/**
 * Classe abstraite immutable qui effectue une command
 * Implemente l'ajout d'un client dans un garage (regles metier)
 *
 * Chaque command possede un id
 */
@Getter
public class ClientCreateCommand{
    
    private String idClient;
    private String nomClient;
    private String prenomClient;
    
    public ClientCreateCommand(String id) {

    }
    
    public ClientCreateCommand(String nomClient, String prenomClient) {

        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
    }
}
