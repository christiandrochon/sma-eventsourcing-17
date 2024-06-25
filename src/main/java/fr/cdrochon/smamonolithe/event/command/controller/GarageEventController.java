package fr.cdrochon.smamonolithe.event.command.controller;

import fr.cdrochon.smamonolithe.event.query.services.EventSourcingService;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@RestController
@RequestMapping("/eventSourcing")
public class GarageEventController {
    
    private final EventSourcingService eventSourcingService;
    
    public GarageEventController(EventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    @GetMapping("/garageEvents/{id}")
    public DomainEventStream eventsById(@PathVariable String id){
        return eventSourcingService.eventsByGarageId(id);
    }
}
