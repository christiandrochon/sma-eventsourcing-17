package fr.cdrochon.smamonolithe.vehicule.command.controllers;

import fr.cdrochon.smamonolithe.vehicule.query.services.VehiculeEventSourcingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Vehicules - Events", description = "Accès aux événements liés aux véhicules")
@RestController
@RequestMapping("/eventSourcing")
public class VehiculeEventController {
    
    private final VehiculeEventSourcingService eventSourcingService;
    
    public VehiculeEventController(VehiculeEventSourcingService eventSourcingService) {
        this.eventSourcingService = eventSourcingService;
    }
    
    /**
     * Recupere un event identifié par son id
     *
     * @param id id du vehicule
     * @return DomainEventStream
     */
    @Operation(
            summary = "Récupérer les événements d'un véhicule",
            description = "Retourne les événements stockés dans l'EventStore Axon pour un véhicule donné."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Événements trouvés"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @GetMapping("/vehiculeEvents/{id}")
    public DomainEventStream eventsById(@PathVariable String id) {
        return eventSourcingService.eventsByVehiculeId(id);
    }
}
