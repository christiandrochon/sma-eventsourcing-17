package fr.cdrochon.smamonolithe.client.command.controller;

import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import fr.cdrochon.smamonolithe.client.command.services.ClientCommandService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.stream.Stream;

@RestController
@Slf4j
@RequestMapping("/commands")
public class ClientCommandController {
    
    private final ClientCommandService clientCommandService;
    private final EventStore eventStore;
    
    
    public ClientCommandController(ClientCommandService clientCommandService, EventStore eventStore) {
        this.clientCommandService = clientCommandService;
        this.eventStore = eventStore;
    }
    
    
    /**
     * Création d'un client de manière asynchrone
     *
     * @param clientCommandDTO DTO de création d'un client
     * @return ResponseEntity<ClientCommandDTO> DTO de création d'un client
     */
    @PostMapping("/createClient")
    public Mono<ResponseEntity<ClientCommandDTO>> createClientAsync(@RequestBody ClientCommandDTO clientCommandDTO) {
        return Mono.fromFuture(clientCommandService.createClient(clientCommandDTO)).subscribeOn(Schedulers.boundedElastic())
                   .flatMap(client -> {
                       log.info("Garage créé : " + client);
                       return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(client));
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
    @GetMapping(path = "/eventStoreClient/{id}") //consumes = MediaType.TEXT_EVENT_STREAM_VALUE
    public Stream readClientsInEventStore(@PathVariable String id) {
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
