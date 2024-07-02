package fr.cdrochon.smamonolithe.garage.query.controllers;

import fr.cdrochon.smamonolithe.garage.query.services.GarageEventSourcingService;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@RestController
@RequestMapping("/eventSourcing")
public class GarageEventSourcingRestController {
    private final GarageEventSourcingService eventSourcingService;
    
    public GarageEventSourcingRestController(GarageEventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    
    /**
     * Renvoi l'ensemble des events pour un garageQuery recupéré avec son id
     *
     * @param id
     * @return
     */
    @GetMapping("/garageQueries/{id}")
//    @PreAuthorize("hasAuthority('USER')")
    public Stream eventsByAccountId(@PathVariable String id) {
        return eventSourcingService.eventsByGarageId(id).asStream();
    }
}
