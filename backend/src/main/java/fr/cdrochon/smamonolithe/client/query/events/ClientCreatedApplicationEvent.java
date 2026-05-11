package fr.cdrochon.smamonolithe.client.query.events;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import org.springframework.context.ApplicationEvent;

/**
 * Event Spring publié après que le ClientCreatedEvent Axon a été persiste en DB.
 * Permet aux services Command de compléter leur CompletableFuture.
 */
public class ClientCreatedApplicationEvent extends ApplicationEvent {
    private final ClientQueryDTO client;

    public ClientCreatedApplicationEvent(Object source, ClientQueryDTO client) {
        super(source);
        this.client = client;
    }

    public ClientQueryDTO getClient() {
        return client;
    }
}


