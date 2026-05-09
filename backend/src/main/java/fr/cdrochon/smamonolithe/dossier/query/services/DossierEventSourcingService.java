package fr.cdrochon.smamonolithe.dossier.query.services;

import org.axonframework.eventsourcing.eventstore.DomainEventStream;

public interface DossierEventSourcingService {
    
    DomainEventStream eventsByDossierId(String id);
}
