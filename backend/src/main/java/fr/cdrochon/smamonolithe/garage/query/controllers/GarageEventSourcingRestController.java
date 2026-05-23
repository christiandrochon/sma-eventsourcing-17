package fr.cdrochon.smamonolithe.garage.query.controllers;

import fr.cdrochon.smamonolithe.garage.query.services.GarageEventSourcingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@Tag(name = "Garages - Event Sourcing", description = "Accès aux événements stockés pour les garages")
@RestController
@RequestMapping("/eventSourcing")
public class GarageEventSourcingRestController {
    
    private final GarageEventSourcingService eventSourcingService;
    
    public GarageEventSourcingRestController(GarageEventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    
    /**
     * Renvoi l'ensemble des events pour un garageQuery recupéré avec son id
     *
     * @param id id du garage
     * @return Stream
     */
    @Operation(
            summary = "Récupérer les événements d'un garage",
            description = "Retourne les événements stockés dans l'EventStore Axon pour un garage donné."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Événements trouvés"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @GetMapping("/garageQueries/{id}")
    public Stream eventsByAccountId(@PathVariable String id) {
        return eventSourcingService.eventsByGarageId(id).asStream();
    }
}
