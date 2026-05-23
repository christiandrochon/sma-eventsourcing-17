package fr.cdrochon.smamonolithe.client.query.controllers;

import fr.cdrochon.smamonolithe.client.query.services.ClientEventSourcingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@Tag(name = "Clients - Event Sourcing", description = "Accès aux événements dans l'event store pour les clients")
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
    @Operation(
            summary = "Récupérer les événements de requête d'un client",
            description = "Retourne le flux des événements de requête pour un client spécifique."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Événements trouvés"),
            @ApiResponse(responseCode = "404", description = "Client non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @GetMapping("/clientQueries/{id}")
    public Stream eventsByAccountId(@PathVariable String id) {
        return eventSourcingService.eventsByClientId(id).asStream();
    }
}
