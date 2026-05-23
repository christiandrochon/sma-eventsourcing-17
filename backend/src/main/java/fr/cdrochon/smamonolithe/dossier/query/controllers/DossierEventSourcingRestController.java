package fr.cdrochon.smamonolithe.dossier.query.controllers;

import fr.cdrochon.smamonolithe.dossier.query.services.DossierEventSourcingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

@Tag(name = "Dossiers - Event Sourcing", description = "Accès aux événements dans l'event store pour les dossiers")
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
     * @param id id
     * @return Stream
     */
    @Operation(
            summary = "Récupérer les événements de requête d'un dossier",
            description = "Retourne le flux des événements de requête pour un dossier spécifique."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Événements trouvés"),
            @ApiResponse(responseCode = "404", description = "Dossier non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @GetMapping("/dossierQueries/{id}")
    public Stream eventsByAccountId(@PathVariable String id) {
        return eventSourcingService.eventsByDossierId(id).asStream();
    }
}
