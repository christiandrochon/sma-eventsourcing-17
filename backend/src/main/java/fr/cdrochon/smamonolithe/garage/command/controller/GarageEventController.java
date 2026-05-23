package fr.cdrochon.smamonolithe.garage.command.controller;

import fr.cdrochon.smamonolithe.garage.query.services.GarageEventSourcingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Garages - Events", description = "Accès aux événements liés aux garages")
@RestController
@RequestMapping("/eventSourcing")
public class GarageEventController {
    
    private final GarageEventSourcingService eventSourcingService;
    
    public GarageEventController(GarageEventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    
    /**
     * Recupere un event identifié par son id
     * @param id id du garage
     * @return DomainEventStream
     */
    @Operation(
            summary = "Récupérer les événements d'un garage",
            description = "Retourne les événements stockés dans l'EventStore Axon pour un garage donné."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Événements trouvés"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @GetMapping("/garageEvents/{id}")
    public DomainEventStream eventsById(@PathVariable String id){
        return eventSourcingService.eventsByGarageId(id);
    }
}
