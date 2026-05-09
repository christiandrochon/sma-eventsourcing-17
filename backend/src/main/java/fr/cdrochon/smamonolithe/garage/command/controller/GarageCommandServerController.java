package fr.cdrochon.smamonolithe.garage.command.controller;

import fr.cdrochon.smamonolithe.garage.command.dtos.GarageCommandDTO;
import fr.cdrochon.smamonolithe.garage.command.services.GarageCommandService;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
        BusinessLoggers.business().info("BIZ_GARAGE_CREATE_REQUEST nomGarage={} mailResp={}",
                                        garageRestPostDTO.getNomGarage(), garageRestPostDTO.getMailResp());
        return Mono.fromFuture(garageCommandService.createGarage(garageRestPostDTO))
                   .flatMap(garageDTO -> {
                       BusinessLoggers.business().info("BIZ_GARAGE_CREATE_OK garageId={} nomGarage={} mailResp={}",
                                                       garageDTO.getId(), garageDTO.getNomGarage(), garageDTO.getMailResp());
                       return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(garageDTO));
                   })
                   .onErrorResume(ex -> {
                       BusinessLoggers.business().error("BIZ_GARAGE_CREATE_FAILED nomGarage={} message={}",
                                                        garageRestPostDTO.getNomGarage(), ex.getMessage());
                       return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                   });
    }
}
