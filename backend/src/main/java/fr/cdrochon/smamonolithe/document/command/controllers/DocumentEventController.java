package fr.cdrochon.smamonolithe.document.command.controllers;

import fr.cdrochon.smamonolithe.document.query.services.DocumentEventSourcingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Documents - Events", description = "Accès aux événements liés aux documents")
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
    @Operation(
            summary = "Récupérer les événements d'un document",
            description = "Retourne les événements stockés dans l'EventStore Axon pour un document donné."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Événements trouvés"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @GetMapping("/documentEvents/{id}")
    public DomainEventStream eventsById(@PathVariable String id) {
        return eventSourcingService.eventsByDocumentId(id);
    }
}
