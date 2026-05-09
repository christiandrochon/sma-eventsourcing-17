package fr.cdrochon.smamonolithe.dossier.command.controller;

import fr.cdrochon.smamonolithe.dossier.query.services.DossierEventSourcingService;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eventSourcing")
public class DossierEventController {
    
    private final DossierEventSourcingService eventSourcingService;
    
    public DossierEventController(DossierEventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    
    /**
     * Recupere un event identifié par son id
     *
     * @param id id du dossier
     * @return DomainEventStream
     */
    @GetMapping("/dossierEvents/{id}")
    public DomainEventStream eventsById(@PathVariable String id) {
        return eventSourcingService.eventsByDossierId(id);
    }
}
