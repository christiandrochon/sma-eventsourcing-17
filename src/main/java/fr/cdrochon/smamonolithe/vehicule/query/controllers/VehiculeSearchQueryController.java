package fr.cdrochon.smamonolithe.vehicule.query.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import fr.cdrochon.smamonolithe.json.Views;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.query.mapper.VehiculeQueryMapper;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(path = "/queries")
@Slf4j
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
    @JsonView(Views.VehiculeView.class)
    public Mono<ResponseEntity<?>> getVehiculeByImmatriculationAsync(@PathVariable String immatriculation) {
        return Mono.fromSupplier(() -> {
                       Vehicule vehicule = vehiculeRepository.findByImmatriculationVehicule(immatriculation);
                       if(vehicule == null) {
                           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                       }
                       VehiculeQueryDTO vehiculeQueryDTO = VehiculeQueryMapper.convertVehiculeToVehiculeDTO(vehicule);
                       log.info("VehiculeQueryDTO trouvé : {}", vehiculeQueryDTO);
                       return ResponseEntity.status(HttpStatus.OK).body(vehiculeQueryDTO);
                   })
                   .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)));
    }
}
