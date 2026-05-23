package fr.cdrochon.smamonolithe.client.command.controller;

import fr.cdrochon.smamonolithe.client.query.services.ClientEventSourcingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Clients - Event Sourcing", description = "Accès aux événements dans l'event store pour les clients")
@RestController
@RequestMapping("/eventSourcing")
public class ClientEventController {
    
    private final ClientEventSourcingService eventSourcingService;
    
    public ClientEventController(ClientEventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    
    /**
     * Recupere un event identifié par son id
     *
     * @param id id du client
     * @return DomainEventStream
     */
    @Operation(
            summary = "Récupérer les événements d'un client",
            description = "Retourne tous les événements du domaine stockés pour un client spécifique."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Événements trouvés"),
            @ApiResponse(responseCode = "404", description = "Client non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @GetMapping("/clientEvents/{id}")
    public DomainEventStream eventsById(@PathVariable String id) {
        return eventSourcingService.eventsByClientId(id);
    }
}
