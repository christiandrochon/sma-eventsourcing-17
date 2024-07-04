package fr.cdrochon.smamonolithe.document.query.services;

import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.stereotype.Service;

@Service
public class DocumentEventSourcingServiceImpl implements DocumentEventSourcingService{
    
    private final EventStore eventStore;
    
    public DocumentEventSourcingServiceImpl(EventStore eventStore) {
        this.eventStore = eventStore;
    }
    
    @Override
    public DomainEventStream eventsByDocumentId(String id) {
        return eventStore.readEvents(id);
    }
}
