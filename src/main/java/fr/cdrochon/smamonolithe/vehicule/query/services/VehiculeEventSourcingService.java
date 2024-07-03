package fr.cdrochon.smamonolithe.vehicule.query.services;

import org.axonframework.eventsourcing.eventstore.DomainEventStream;

public interface VehiculeEventSourcingService {
    
    DomainEventStream eventsByVehiculeId(String id);
}
