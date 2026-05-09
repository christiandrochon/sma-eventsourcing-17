package fr.cdrochon.smamonolithe.document.query.services;

import org.axonframework.eventsourcing.eventstore.DomainEventStream;

public interface DocumentEventSourcingService {
    
    DomainEventStream eventsByDocumentId(String id);
}
