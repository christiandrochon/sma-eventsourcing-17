package fr.cdrochon.smamonolithe.vehicule.query.controllers;

import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.query.mapper.VehiculeQueryMapper;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(path = "/queries")
public class VehiculeSearchQueryController {
    
    private final VehiculeRepository vehiculeRepository;
    
    public VehiculeSearchQueryController(VehiculeRepository vehiculeRepository) {
        this.vehiculeRepository = vehiculeRepository;
    }
    
    /**
     * Empêche la création d'un vehicule si l'immatriculation existe déjà. Vérifie si un vehicule existe en fonction de son immatriculation.
     *
     * @param immatriculation immatriculation du vehicule
     * @return Boolean
     */
    @GetMapping("/vehiculeExists/{immatriculation}")
    public Mono<Boolean> immatriculationExiste(@PathVariable String immatriculation) {
        CompletableFuture<Boolean> future =
                CompletableFuture.supplyAsync(() -> vehiculeRepository.existsByImmatriculationVehicule(immatriculation));
        Mono<Boolean> mono = Mono.fromFuture(future);
        return mono;
    }
//    public Boolean immatriculationExiste(@PathVariable String immatriculation) {
//        return vehiculeRepository.existsByImmatriculationVehicule(immatriculation);
//    }
    
    /**
     * Renvoi les informations considérées comme utiles à la partie query lors de la recherche d'un vehicule par son immatriculation.
     *
     * @param immatriculation immatriculation du vehicule
     * @return VehiculeResponseDTO
     */
    @GetMapping(value = "/vehicules/immatriculation/{immatriculation}")
    public Mono<VehiculeQueryDTO> getVehiculeByImmatriculationAsync(@PathVariable String immatriculation) {
        CompletableFuture<VehiculeQueryDTO> future =
                CompletableFuture.supplyAsync(() -> {
                    Vehicule vehicule = vehiculeRepository.findByImmatriculationVehicule(immatriculation);
                    return VehiculeQueryMapper.convertVehiculeToVehiculeDTO(vehicule);
                });
        Mono<VehiculeQueryDTO> mono = Mono.fromFuture(future);
        return mono;
    }
}
