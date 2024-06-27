package fr.cdrochon.smamonolithe.garage.command.commands;

import lombok.Getter;

/**
 * Classe abstraite immutable qui effectue une command
 * Implemente l'ajout d'un client dans un garage (regles metier)
 * <p>
 * Chaque command possede un id
 */
@Getter
public class ClientCreateCommand extends BaseCommand<String> {

    private String nomClient;
    private String prenomClient;
    
    public ClientCreateCommand(String id) {
    super(id);
    }
    
    public ClientCreateCommand(String id, String nomClient, String prenomClient) {
        super(id);
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
    }
}
