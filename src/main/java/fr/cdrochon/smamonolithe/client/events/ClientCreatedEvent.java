package fr.cdrochon.smamonolithe.client.events;

import fr.cdrochon.smamonolithe.client.query.entities.ClientStatus;
import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import lombok.Getter;
@Getter
public class ClientCreatedEvent extends ClientBaseEvent<String> {
    
    private final String nomClient;
    private final String prenomClient;
    private final String mailClient;
    private final String telClient;
    private final AdresseClient adresseClient;
    private final ClientStatus clientStatus;
    
    public ClientCreatedEvent(String id, String nomClient, String prenomClient, String mailClient, String telClient, AdresseClient adresseClient,
                              ClientStatus clientStatus) {
        super(id);
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.mailClient = mailClient;
        this.telClient = telClient;
        this.adresseClient = adresseClient;
        this.clientStatus = clientStatus;
    }
    
}
