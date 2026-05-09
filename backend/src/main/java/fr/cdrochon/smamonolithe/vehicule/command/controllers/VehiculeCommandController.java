package fr.cdrochon.smamonolithe.vehicule.command.controllers;

import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
import fr.cdrochon.smamonolithe.vehicule.command.services.VehiculeCommandService;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/commands")
public class VehiculeCommandController {
    
    private final VehiculeCommandService vehiculeCommandService;
    
    public VehiculeCommandController(VehiculeCommandService vehiculeCommandService) {
        this.vehiculeCommandService = vehiculeCommandService;
        
    }
    
    /**
     * Création d'un vehicule de manière asynchrone
     *
     * @param vehiculeRequestDTO DTO de création d'un vehicule
     * @return ResponseEntity<VehiculeCommandDTO> DTO de création d'un vehicule
     */
    /**
     * Création d'un vehicule : ADMIN ou USER.
     * Note : la restriction "son propre véhicule uniquement" (pour USER) est à appliquer au niveau service.
     */
    @PostMapping(value = "/createVehicule")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Mono<ResponseEntity<VehiculeCommandDTO>> createClientAsync(@RequestBody VehiculeCommandDTO vehiculeRequestDTO) {
        BusinessLoggers.business().info("BIZ_VEHICULE_CREATE_REQUEST immatriculation={} status={}",
                                        vehiculeRequestDTO.getImmatriculationVehicule(), vehiculeRequestDTO.getVehiculeStatus());
        return Mono.fromFuture(vehiculeCommandService.createVehicule(vehiculeRequestDTO)).subscribeOn(Schedulers.boundedElastic())
                   .flatMap(vehicule -> {
                       BusinessLoggers.business().info("BIZ_VEHICULE_CREATE_OK vehiculeId={} immatriculation={} status={}",
                                                       vehicule.getId(), vehicule.getImmatriculationVehicule(), vehicule.getVehiculeStatus());
                       return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(vehicule));
                   })
                   .onErrorResume(ex -> {
                       BusinessLoggers.business().error("BIZ_VEHICULE_CREATE_FAILED immatriculation={} message={}",
                                                        vehiculeRequestDTO.getImmatriculationVehicule(), ex.getMessage());
                       return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                   });
    }
}
