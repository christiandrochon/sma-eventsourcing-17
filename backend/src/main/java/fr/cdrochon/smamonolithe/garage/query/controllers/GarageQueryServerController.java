package fr.cdrochon.smamonolithe.garage.query.controllers;

import fr.cdrochon.smamonolithe.garage.query.dto.GarageQueryDTO;
import fr.cdrochon.smamonolithe.garage.query.mapper.GarageMapperManuel;
import fr.cdrochon.smamonolithe.garage.query.repositories.GarageRepository;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Tag(name = "Garages - Queries", description = "Requêtes de lecture CQRS liées aux garages")
@RestController
@RequestMapping(path = "/queries")
public class GarageQueryServerController {
    
    private final GarageRepository garageRepository;
    
    public GarageQueryServerController(GarageRepository garageRepository) {
        this.garageRepository = garageRepository;
    }
    
    /**
     * <p>Méthode asynchrone qui renvoi un garage dto. </p>
     * <p>L'appel à la base de données lui meme est synchrone, mais CompletableFuture effectue une
     * operation asynchrone. Mono va réagir à la fin de l'appel en s'appuyant sur CompletableFuture qui va gérer le mécanisme de Thread).
     *
     * @param id id du garage
     * @return Mono de GarageResponseDTO
     */
    @Operation(
            summary = "Récupérer un garage par ID",
            description = "Récupère les informations d'un garage spécifique."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Garage trouvé",
                    content = @Content(schema = @Schema(implementation = GarageQueryDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Garage non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @GetMapping(path = "/garages/{id}")
    public Mono<GarageQueryDTO> getGarageByIdAsync(@PathVariable String id) {
        BusinessLoggers.business().info("BIZ_GARAGE_READ_REQUEST garageId={}", id);
        CompletableFuture<GarageQueryDTO> future =
                CompletableFuture.supplyAsync(() -> {
                    GarageQueryDTO garage = garageRepository.findById(id)
                                                            .map(GarageMapperManuel::convertGarageToGarageDTO)
                                                            .orElse(null);
                    if(garage == null) {
                        BusinessLoggers.business().info("BIZ_GARAGE_READ_NOT_FOUND garageId={}", id);
                    } else {
                        BusinessLoggers.business().info("BIZ_GARAGE_READ_SUCCESS garageId={}", id);
                    }
                    return garage;
                });
        Mono<GarageQueryDTO> mono = Mono.fromFuture(future);
        return mono;
    }
    
    /**
     * Retournes la liste de tous les garages de manière asynchrone
     *
     * @return Flux de GarageResponseDTO
     */
    @Operation(
            summary = "Lister tous les garages",
            description = "Récupère la liste de tous les garages disponibles."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des garages",
                    content = @Content(schema = @Schema(implementation = GarageQueryDTO.class))
            ),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @GetMapping(path = "/garages")
    public Flux<GarageQueryDTO> getGaragesAsyncServer() {
        BusinessLoggers.business().info("BIZ_GARAGE_LIST_REQUEST");
        CompletableFuture<List<GarageQueryDTO>> future =
                CompletableFuture.supplyAsync(() -> {
                    List<GarageQueryDTO> garages =
                            garageRepository.findAll()
                                            .stream()
                                            .map(GarageMapperManuel::convertGarageToGarageDTO)
                                            .collect(Collectors.toList());
                    BusinessLoggers.business().info("BIZ_GARAGE_LIST_SUCCESS count={}", garages.size());
                    return garages;
                });
        Flux<GarageQueryDTO> flux = Flux.fromStream(future.join().stream());
        return flux;
    }
}
