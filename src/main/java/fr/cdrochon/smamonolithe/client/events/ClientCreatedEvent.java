package fr.cdrochon.smamonolithe.client.events;

import fr.cdrochon.smamonolithe.client.command.enums.ClientStatus;
import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import lombok.Getter;

public class ClientCreatedEvent {
    
    @Getter private final String nomClient;
    @Getter private final String prenomClient;
    @Getter private final String mailClient;
    @Getter private final String telClient;
    @Getter private final AdresseClient adresseClient;
    @Getter private final ClientStatus clientStatus;
    
    public ClientCreatedEvent(String nomClient, String prenomClient, String mailClient, String telClient, AdresseClient adresseClient, ClientStatus clientStatus) {
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.mailClient = mailClient;
        this.telClient = telClient;
        this.adresseClient = adresseClient;
        this.clientStatus = clientStatus;
    }
}
