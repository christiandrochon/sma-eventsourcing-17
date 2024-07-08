package fr.cdrochon.smamonolithe.dossier.query.services;

import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.stereotype.Service;

@Service
public class DossierEventSourcingServiceImpl implements DossierEventSourcingService{
    
    private final EventStore eventStore;
    
    public DossierEventSourcingServiceImpl(EventStore eventStore) {
        this.eventStore = eventStore;
    }
    
    @Override
    public DomainEventStream eventsByDossierId(String id) {
        return eventStore.readEvents(id);
    }
}
