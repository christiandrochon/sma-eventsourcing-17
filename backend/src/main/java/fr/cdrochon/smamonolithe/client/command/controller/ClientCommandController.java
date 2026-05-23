package fr.cdrochon.smamonolithe.client.command.controller;

import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import fr.cdrochon.smamonolithe.client.command.services.ClientCommandService;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.stream.Stream;

@Tag(name = "Clients - Commands", description = "Commandes CQRS liées aux clients")
@RestController
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
    /**
     * Création d'un client : réservé à l'ADMIN.
     */
    @Operation(
            summary = "Créer un client",
            description = "Crée un client via une commande Axon. Endpoint réservé au rôle ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Client créé",
                    content = @Content(schema = @Schema(implementation = ClientCommandDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @PostMapping("/createClient")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ClientCommandDTO>> createClientAsync(@RequestBody ClientCommandDTO clientCommandDTO) {
        BusinessLoggers.business().info("BIZ_CLIENT_CREATE_REQUEST nomClient={} prenomClient={} status={}",
                                        clientCommandDTO.getNomClient(), clientCommandDTO.getPrenomClient(), clientCommandDTO.getClientStatus());
        return Mono.fromFuture(clientCommandService.createClient(clientCommandDTO)).subscribeOn(Schedulers.boundedElastic())
                   .flatMap(client -> {
                       BusinessLoggers.business().info("BIZ_CLIENT_CREATE_OK clientId={} nomClient={} prenomClient={} status={}",
                                                       client.getId(), client.getNomClient(), client.getPrenomClient(), client.getClientStatus());
                       return Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(client));
                   })
                   .onErrorResume(ex -> {
                       BusinessLoggers.business().error("BIZ_CLIENT_CREATE_FAILED nomClient={} prenomClient={} message={}",
                                                        clientCommandDTO.getNomClient(), clientCommandDTO.getPrenomClient(), ex.getMessage());
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
    @Operation(
            summary = "Lire les événements d’un client",
            description = "Retourne les événements stockés dans l’EventStore Axon pour un aggregate id donné."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Événements trouvés"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
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
