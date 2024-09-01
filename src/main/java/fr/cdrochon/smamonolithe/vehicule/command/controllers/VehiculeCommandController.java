package fr.cdrochon.smamonolithe.vehicule.command.controllers;

import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
import fr.cdrochon.smamonolithe.vehicule.command.services.VehiculeCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@RequestMapping("/commands")
public class VehiculeCommandController {
    
    private final VehiculeCommandService vehiculeCommandService;
    
    public VehiculeCommandController(VehiculeCommandService vehiculeCommandService) {
        this.vehiculeCommandService = vehiculeCommandService;
        
    }
    
    /**
     * Création d'un vehicule de manière asynchrone.
     * <p>
     * Le Schedulers.boundElastic() va permettre de prendre en charge d'eventuelles taches bloquantes sur =une requete POST. A noter que la tache
     * commandGateway. send() est une methode non bloquante.
     *
     * @param vehiculeRequestDTO DTO de création d'un vehicule
     * @return ResponseEntity<VehiculeCommandDTO> DTO de création d'un vehicule
     */
    @PostMapping(value = "/createVehicule")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<ResponseEntity<VehiculeCommandDTO>> createClientAsync(@RequestBody VehiculeCommandDTO vehiculeRequestDTO) {
        return Mono.fromFuture(() -> vehiculeCommandService.createVehicule(vehiculeRequestDTO)).subscribeOn(Schedulers.boundedElastic())
                   .flatMap(vehicule -> {
                       log.info("Garage créé : " + vehicule);
                       return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(vehicule));
                   })
                   .onErrorResume(ex -> {
                       log.error("Erreur lors de la création du garage : " + ex.getMessage());
                       return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                   });
    }
}
