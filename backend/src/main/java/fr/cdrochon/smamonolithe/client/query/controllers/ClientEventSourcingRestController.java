package fr.cdrochon.smamonolithe.client.query.controllers;

import fr.cdrochon.smamonolithe.client.query.services.ClientEventSourcingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@RestController
@RequestMapping("/eventSourcing")
public class ClientEventSourcingRestController {
    
    private final ClientEventSourcingService eventSourcingService;
    
    public ClientEventSourcingRestController(ClientEventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    
    /**
     * Renvoi l'ensemble des events pour un clientQuery recupéré avec son id
     *
     * @param id id du clientQuery
     * @return Stream
     */
    @GetMapping("/clientQueries/{id}")
    public Stream eventsByAccountId(@PathVariable String id) {
        return eventSourcingService.eventsByClientId(id).asStream();
    }
}
