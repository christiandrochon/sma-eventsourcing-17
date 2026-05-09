package fr.cdrochon.smamonolithe.vehicule.query.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import fr.cdrochon.smamonolithe.json.Views;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
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
        BusinessLoggers.business().info("BIZ_VEHICULE_READ_BY_IMMATRICULATION_REQUEST immatriculation={}", immatriculation);
        CompletableFuture<Boolean> future =
                CompletableFuture.supplyAsync(() -> {
                    boolean exists = vehiculeRepository.existsByImmatriculationVehicule(immatriculation);
                    BusinessLoggers.business().info("BIZ_VEHICULE_READ_BY_IMMATRICULATION_SUCCESS immatriculation={} exists={}",
                                                    immatriculation,
                                                    exists);
                    return exists;
                });
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
        BusinessLoggers.business().info("BIZ_VEHICULE_READ_BY_IMMATRICULATION_REQUEST immatriculation={}", immatriculation);
        return Mono.fromSupplier(() -> {
                       Vehicule vehicule = vehiculeRepository.findByImmatriculationVehicule(immatriculation);
                       if(vehicule == null) {
                           BusinessLoggers.business().info("BIZ_VEHICULE_READ_BY_IMMATRICULATION_NOT_FOUND immatriculation={}",
                                                           immatriculation);
                           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                       }
                       VehiculeQueryDTO vehiculeQueryDTO = VehiculeQueryMapper.convertVehiculeToVehiculeDTO(vehicule);
                       BusinessLoggers.business().info("BIZ_VEHICULE_READ_BY_IMMATRICULATION_SUCCESS immatriculation={} vehiculeId={}",
                                                       immatriculation,
                                                       vehiculeQueryDTO.getId());
                       log.info("VehiculeQueryDTO trouvé : {}", vehiculeQueryDTO);
                       return ResponseEntity.status(HttpStatus.OK).body(vehiculeQueryDTO);
                   })
                   .onErrorResume(e -> {
                       BusinessLoggers.business().error("BIZ_VEHICULE_READ_BY_IMMATRICULATION_FAILED immatriculation={} message={}",
                                                       immatriculation,
                                                       e.getMessage());
                       return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
                   });
    }
}
