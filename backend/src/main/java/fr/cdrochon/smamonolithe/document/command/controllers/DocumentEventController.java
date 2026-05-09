package fr.cdrochon.smamonolithe.document.command.controllers;

import fr.cdrochon.smamonolithe.document.query.services.DocumentEventSourcingService;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eventSourcing")
public class DocumentEventController {
    
    private final DocumentEventSourcingService eventSourcingService;
    
    public DocumentEventController(DocumentEventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    
    /**
     * Recupere un event identifié par son id
     *
     * @param id id du document
     * @return DomainEventStream
     */
    @GetMapping("/documentEvents/{id}")
    public DomainEventStream eventsById(@PathVariable String id) {
        return eventSourcingService.eventsByDocumentId(id);
    }
}
