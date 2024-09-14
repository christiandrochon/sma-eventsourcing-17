package fr.cdrochon.smamonolithe.document.query.controllers;

import fr.cdrochon.smamonolithe.document.query.services.DocumentEventSourcingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@RestController
@RequestMapping("/eventSourcing")
public class DocumentEventSourcingRestController {
    
    private final DocumentEventSourcingService eventSourcingService;
    
    public DocumentEventSourcingRestController(DocumentEventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    
    /**
     * Renvoi l'ensemble des events pour un document recupéré avec son id
     *
     * @param id id du document
     * @return Stream
     */
    @GetMapping("/documentQueries/{id}")
    public Stream eventsByDocumentId(@PathVariable String id) {
        return eventSourcingService.eventsByDocumentId(id).asStream();
    }
}
