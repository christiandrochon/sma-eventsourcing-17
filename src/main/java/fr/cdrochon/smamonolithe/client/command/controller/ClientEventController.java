package fr.cdrochon.smamonolithe.client.command.controller;

import fr.cdrochon.smamonolithe.client.query.services.ClientEventSourcingService;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eventSourcing")
public class ClientEventController {
    
    private final ClientEventSourcingService eventSourcingService;
    
    public ClientEventController(ClientEventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    
    /**
     * Recupere un event identifié par son id
     * @param id
     * @return DomainEventStream
     */
    @GetMapping("/clientEvents/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public DomainEventStream eventsById(@PathVariable String id){
        return eventSourcingService.eventsByClientId(id);
    }
}
