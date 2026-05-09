package fr.cdrochon.smamonolithe.vehicule.command.controllers;

import fr.cdrochon.smamonolithe.vehicule.query.services.VehiculeEventSourcingService;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eventSourcing")
public class VehiculeEventController {
    
    private final VehiculeEventSourcingService eventSourcingService;
    
    public VehiculeEventController(VehiculeEventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    
    /**
     * Recupere un event identifié par son id
     *
     * @param id id du vehicule
     * @return DomainEventStream
     */
    @GetMapping("/vehiculeEvents/{id}")
    public DomainEventStream eventsById(@PathVariable String id) {
        return eventSourcingService.eventsByVehiculeId(id);
    }
}
