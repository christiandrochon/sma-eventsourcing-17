package fr.cdrochon.smamonolithe.garage.query.services;

import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.stereotype.Service;

@Service
public class GarageEventSourcingServiceImpl implements GarageEventSourcingService {
    
    private final EventStore eventStore;
    
    public GarageEventSourcingServiceImpl(EventStore eventStore) {
        this.eventStore = eventStore;
    }
    
    @Override
    public DomainEventStream eventsByGarageId(String id) {
        DomainEventStream domainEventStream = eventStore.readEvents(id);
        return domainEventStream;
    }
}
