package fr.cdrochon.smamonolithe.dossier.query.controllers;

import fr.cdrochon.smamonolithe.dossier.query.services.DossierEventSourcingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@RestController
@RequestMapping("/eventSourcing")
public class DossierEventSourcingRestController {
    
    private final DossierEventSourcingService eventSourcingService;
    
    public DossierEventSourcingRestController(DossierEventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    
    /**
     * Renvoi l'ensemble des events pour un dossier recupéré avec son id
     *
     * @param id
     * @return Stream
     */
    @GetMapping("/dossierQueries/{id}")
    public Stream eventsByAccountId(@PathVariable String id) {
        return eventSourcingService.eventsByDossierId(id).asStream();
    }
}
