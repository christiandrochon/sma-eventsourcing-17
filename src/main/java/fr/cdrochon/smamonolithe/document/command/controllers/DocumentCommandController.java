package fr.cdrochon.smamonolithe.document.command.controllers;

import fr.cdrochon.smamonolithe.document.command.dtos.DocumentCommandDTO;
import fr.cdrochon.smamonolithe.document.command.services.DocumentCommandService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

@RestController
@Slf4j
@RequestMapping("/commands")
public class DocumentCommandController {
    
    private final EventStore eventStore;
    
    private final DocumentCommandService documentCommandService;
    
    public DocumentCommandController(DocumentCommandService documentCommandService, EventStore eventStore) {
        this.eventStore = eventStore;
        this.documentCommandService = documentCommandService;
    }
    
    /**
     * Création d'un document de manière asynchrone
     *
     * @param documentCommandDTO DTO de création d'un document
     * @return ResponseEntity<DocumentCommandDTO> DTO de création d'un document
     */
    @PostMapping(value = "/createDocument")
    //    @PreAuthorize("hasRole('USER')")
    //    @PreAuthorize("hasAuthority('USER')")
    public Mono<ResponseEntity<DocumentCommandDTO>> createClientAsync(@RequestBody DocumentCommandDTO documentCommandDTO) {
        return Mono.fromFuture(documentCommandService.createDocument(documentCommandDTO))
                   .flatMap(document -> {
                       log.info("Garage créé : " + document.getId());
                       return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(document));
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
    @GetMapping(path = "/eventStoreDocument/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public Stream readDocumentsInEventStore(@PathVariable String id) {
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
