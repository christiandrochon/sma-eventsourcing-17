package fr.cdrochon.smamonolithe.document.query.controllers;

import fr.cdrochon.smamonolithe.document.query.services.DocumentEventSourcingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@Tag(name = "Documents - Event Sourcing", description = "Accès aux événements stockés pour les documents")
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
    @Operation(
            summary = "Récupérer les événements d'un document",
            description = "Retourne les événements stockés dans l'EventStore Axon pour un document donné."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Événements trouvés"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @GetMapping("/documentQueries/{id}")
    public Stream eventsByDocumentId(@PathVariable String id) {
        return eventSourcingService.eventsByDocumentId(id).asStream();
    }
}
