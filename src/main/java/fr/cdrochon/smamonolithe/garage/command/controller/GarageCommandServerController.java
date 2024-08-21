package fr.cdrochon.smamonolithe.garage.command.controller;

import fr.cdrochon.smamonolithe.garage.command.dtos.GarageCommandDTO;
import fr.cdrochon.smamonolithe.garage.command.services.GarageCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/commands")
public class GarageCommandServerController {
    
    private final GarageCommandService garageCommandService;
    
    public GarageCommandServerController(GarageCommandService garageQueryCommandService) {
        this.garageCommandService = garageQueryCommandService;
    }
    
    /**
     * Création d'un garage de manière asynchrone
     *
     * @param garageRestPostDTO DTO de création d'un garage
     * @return ResponseEntity<GarageCommandDTO> DTO de création d'un garage
     */
    @PostMapping("/{createGarage}")
    public Mono<ResponseEntity<GarageCommandDTO>> createGarage(@RequestBody GarageCommandDTO garageRestPostDTO) {
        return Mono.fromFuture(garageCommandService.createGarage(garageRestPostDTO))
                   .flatMap(garageDTO -> {
                       log.info("Garage créé : " + garageDTO);
                       return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(garageDTO));
                   })
                   .onErrorResume(ex -> {
                       log.error("Erreur lors de la création du garage : " + ex.getMessage());
                       return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                   });
    }
}
