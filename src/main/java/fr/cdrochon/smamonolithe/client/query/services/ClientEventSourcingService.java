package fr.cdrochon.smamonolithe.client.query.services;

import org.axonframework.eventsourcing.eventstore.DomainEventStream;

public interface ClientEventSourcingService {
    
    DomainEventStream eventsByClientId(String id);
}
