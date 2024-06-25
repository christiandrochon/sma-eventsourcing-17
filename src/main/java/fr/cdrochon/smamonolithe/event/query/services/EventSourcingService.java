package fr.cdrochon.smamonolithe.event.query.services;

import org.axonframework.eventsourcing.eventstore.DomainEventStream;

public interface EventSourcingService {
    
    DomainEventStream eventsByGarageId(String id);
}
