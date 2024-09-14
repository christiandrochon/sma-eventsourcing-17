package fr.cdrochon.smamonolithe.vehicule.query.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import fr.cdrochon.smamonolithe.json.Views;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.mapper.VehiculeQueryMapper;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/queries")
public class VehiculeQueryController {
    
    private final VehiculeRepository vehiculeRepository;
    
    public VehiculeQueryController(VehiculeRepository vehiculeRepository) {
        this.vehiculeRepository = vehiculeRepository;
    }
    
    /**
     * Renvoi les informations considérées comme utiles à la partie query lors de la recherche d'un vehicule par son id.
     *
     * @param id id du vehicule
     * @return VehiculeResponseDTO
     */
    @GetMapping("/vehicules/{id}")
    @JsonView(Views.VehiculeView.class)
    //    @CircuitBreaker(name = "clientService", fallbackMethod = "getDefaultClient")
    public Mono<VehiculeQueryDTO> getDocumentByIdAsync(@PathVariable String id) {
        CompletableFuture<VehiculeQueryDTO> future =
                CompletableFuture.supplyAsync(() -> {
                    VehiculeQueryDTO dto = vehiculeRepository.findById(id)
                                                             .map(VehiculeQueryMapper::convertVehiculeToVehiculeDTO)
                                                             .orElse(null);
                    return dto;
                });
        Mono<VehiculeQueryDTO> mono = Mono.fromFuture(future);
        return mono;
    }
    
    
    /**
     * Renvoi tous les vehicules. On n'utilise pas l'interface Repository usuelle, mais on créé une classe destinée à ca, qui renvoi le type de DTO sous forme
     * de multiples instances
     *
     * @return List<VehiculeResponseDTO> comprenant l'adresse sous forme de DTO
     */
    @GetMapping("/vehicules")
    @JsonView(Views.VehiculeView.class)
    public Flux<VehiculeQueryDTO> getDossiersAsync() {
        CompletableFuture<List<VehiculeQueryDTO>> future = CompletableFuture.supplyAsync(() -> {
            List<VehiculeQueryDTO> clients =
                    vehiculeRepository.findAll()
                                      .stream()
                                      .map(VehiculeQueryMapper::convertVehiculeToVehiculeDTO)
                                      .collect(Collectors.toList());
            return clients;
        });
        Flux<VehiculeQueryDTO> flux = Flux.fromStream(future.join().stream());
        return flux;
    }
}
