package fr.cdrochon.smamonolithe.garage.command.controller;

import fr.cdrochon.smamonolithe.garage.query.services.GarageEventSourcingService;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eventSourcing")
public class GarageEventController {
    
    private final GarageEventSourcingService eventSourcingService;
    
    public GarageEventController(GarageEventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    
    /**
     * Recupere un event identifié par son id
     * @param id id du garage
     * @return DomainEventStream
     */
    @GetMapping("/garageEvents/{id}")
    public DomainEventStream eventsById(@PathVariable String id){
        return eventSourcingService.eventsByGarageId(id);
    }
}
