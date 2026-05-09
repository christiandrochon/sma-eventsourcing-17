package fr.cdrochon.smamonolithe.vehicule.query.controllers;

import fr.cdrochon.smamonolithe.client.query.services.ClientEventSourcingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@RestController
@RequestMapping("/eventSourcing")
public class VehiculeEventSourcingRestController {
    
    private final ClientEventSourcingService eventSourcingService;
    
    public VehiculeEventSourcingRestController(ClientEventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    
    /**
     * Renvoi l'ensemble des events pour un vehicule recupéré avec son id
     *
     * @param id id du vehicule
     * @return Stream
     */
    @GetMapping("/vehiculeQueries/{id}")
    public Stream eventsByAccountId(@PathVariable String id) {
        return eventSourcingService.eventsByClientId(id).asStream();
    }
}
