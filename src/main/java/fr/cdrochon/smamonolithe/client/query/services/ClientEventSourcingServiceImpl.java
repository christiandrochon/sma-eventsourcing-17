package fr.cdrochon.smamonolithe.client.query.services;

import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.stereotype.Service;

@Service
public class ClientEventSourcingServiceImpl implements ClientEventSourcingService {
    
    private final EventStore eventStore;
    
    public ClientEventSourcingServiceImpl(EventStore eventStore) {
        this.eventStore = eventStore;
    }
    
    @Override
    public DomainEventStream eventsByClientId(String id) {
        DomainEventStream domainEventStream = eventStore.readEvents(id);
        return domainEventStream;
    }
}
