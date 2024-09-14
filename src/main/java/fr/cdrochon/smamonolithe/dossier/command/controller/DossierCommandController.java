package fr.cdrochon.smamonolithe.dossier.command.controller;

import fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandDTO;
import fr.cdrochon.smamonolithe.dossier.command.services.DossierCommandService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.stream.Stream;

@RestController
@RequestMapping("/commands")
@Slf4j
public class DossierCommandController {
    
    private final EventStore eventStore;
    private final DossierCommandService dossierCommandService;
    
    public DossierCommandController(EventStore eventStore, DossierCommandService dossierCommandService) {
        this.eventStore = eventStore;
        this.dossierCommandService = dossierCommandService;
    }
    
    /**
     * Création d'un dossier de manière asynchrone
     * <p>
     * L'id ne peut pas etre negatif
     *
     * @param dossierCommandDTO DTO de création d'un dossier
     * @return ResponseEntity<DossierCommandDTO> DTO de création d'un dossier
     */
    @PostMapping(value = "/createDossier")
    public Mono<ResponseEntity<DossierCommandDTO>> createClientAsync(@RequestBody DossierCommandDTO dossierCommandDTO) {
        return Mono.fromFuture(dossierCommandService.createDossier(dossierCommandDTO)).subscribeOn(Schedulers.boundedElastic())
                   .flatMap(dossier -> {
                       log.info("Garage créé : " + dossier);
                       return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(dossier));
                   })
                   .onErrorResume(ex -> {
                       log.error("Erreur lors de la création du garage : " + ex.getMessage());
                       return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                   });
    }
    
    /**
     * Tester les events du store. On utilise l'id de l'agregat pour consulter l'etat de l'eventstore (json avec tous les events enregistrés) Le format renvoyé
     * est du json dans swagger
     *
     * @param id id de l'agregat
     * @return Stream
     */
    @GetMapping(path = "/eventStoreDossier/{id}")
    public Stream readDossiersInEventStore(@PathVariable String id) {
        return eventStore.readEvents(id).asStream();
    }
    
    
    /**
     * Pour recuperer les messages d'erreur lorsqu'une requete s'est mal passée
     *
     * @param exception exception
     * @return message d'erreur
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
