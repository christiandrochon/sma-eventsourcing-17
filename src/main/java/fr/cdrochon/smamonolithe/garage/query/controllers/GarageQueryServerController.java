package fr.cdrochon.smamonolithe.garage.query.controllers;

import fr.cdrochon.smamonolithe.garage.query.dto.GarageQueryDTO;
import fr.cdrochon.smamonolithe.garage.query.mapper.GarageMapperManuel;
import fr.cdrochon.smamonolithe.garage.query.repositories.GarageRepository;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
//import reactor.core.publisher.Flux;


@RestController
@RequestMapping(path = "/queries")
//@PreAuthorize("hasAuthority('USER')")
public class GarageQueryServerController {
    
    private final QueryGateway queryGateway;
    private final GarageRepository garageRepository;
    
    public GarageQueryServerController(QueryGateway queryGateway, GarageRepository garageRepository) {
        this.queryGateway = queryGateway;
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
    @GetMapping(path = "/garages/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public Mono<GarageQueryDTO> getGarageByIdAsync(@PathVariable String id) {
        CompletableFuture<GarageQueryDTO> future =
                CompletableFuture.supplyAsync(() -> {
                    GarageQueryDTO garage = garageRepository.findById(id)
                                                            .map(GarageMapperManuel::convertGarageToGarageDTO)
                                                            .orElse(null);
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
    @GetMapping(path = "/garages")
    @PreAuthorize("hasAuthority('USER')")
    public Flux<GarageQueryDTO> getGaragesAsyncServer() {
        CompletableFuture<List<GarageQueryDTO>> future =
                CompletableFuture.supplyAsync(() -> {
                    List<GarageQueryDTO> garages =
                            garageRepository.findAll()
                                            .stream()
                                            .map(GarageMapperManuel::convertGarageToGarageDTO)
                                            .collect(Collectors.toList());
                    return garages;
                });
        Flux<GarageQueryDTO> flux = Flux.fromStream(future.join().stream());
        return flux;
    }
    
    /**
     * Renvoi la liste de tous les garages de manière synchrone, mais en convertissant une List en Flux
     *
     * @return Flux de GarageResponseDTO
     */
    //    @GetMapping(path = "/garages")
    //    @PreAuthorize("hasAuthority('USER')")
    //    public Flux<GarageResponseDTO> getGaragesFlux() {
    //        List<GarageResponseDTO> garages = garageRepository.findAll()
    //                                                          .stream()
    //                                                          .map(GarageMapperManuel::convertGarageToGarageDTO)
    //                                                          .collect(Collectors.toList());
    //        return Flux.fromIterable(garages);
    //    }
}
