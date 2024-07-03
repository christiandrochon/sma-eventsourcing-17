package fr.cdrochon.smamonolithe.vehicule.query.services;

import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.stereotype.Service;

@Service
public class VehiculeEventSourcingServiceImpl implements VehiculeEventSourcingService{
    
    private final EventStore eventStore;
    
    public VehiculeEventSourcingServiceImpl(EventStore eventStore) {
        this.eventStore = eventStore;
    }
    
    @Override
    public DomainEventStream eventsByVehiculeId(String id) {
        DomainEventStream domainEventStream = eventStore.readEvents(id);
        return domainEventStream;
    }
}
