package fr.cdrochon.smamonolithe.event.query.controllers;

import fr.cdrochon.smamonolithe.event.query.services.EventSourcingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@RestController
@RequestMapping("/eventSourcing")
public class GarageQueryEventSourcingRestController {
    private final EventSourcingService eventSourcingService;
    
    public GarageQueryEventSourcingRestController(EventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    
    /**
     * Renvoi l'ensemble des events pour un garageQuery recupéré avec son id
     *
     * @param id
     * @return
     */
    @GetMapping("/garageQueries/{id}")
    public Stream eventsByAccountId(@PathVariable String id) {
        return eventSourcingService.eventsByGarageId(id).asStream();
    }
}
