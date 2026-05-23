package fr.cdrochon.smamonolithe.vehicule.query.controllers;

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

@Tag(name = "Vehicules - Event Sourcing", description = "Accès aux événements stockés pour les véhicules")
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
    @Operation(
            summary = "Récupérer les événements d'un véhicule",
            description = "Retourne les événements stockés dans l'EventStore Axon pour un véhicule donné."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Événements trouvés"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @GetMapping("/vehiculeQueries/{id}")
    public Stream eventsByAccountId(@PathVariable String id) {
        return eventSourcingService.eventsByClientId(id).asStream();
    }
}
