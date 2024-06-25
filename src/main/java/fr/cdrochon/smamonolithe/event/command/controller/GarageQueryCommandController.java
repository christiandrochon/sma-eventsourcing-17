package fr.cdrochon.smamonolithe.event.command.controller;

import fr.cdrochon.smamonolithe.event.command.services.GarageQueryCommandService;
import fr.cdrochon.smamonolithe.event.commonapi.command.GarageQueryCreateCommand;
import fr.cdrochon.smamonolithe.event.commonapi.dto.CreateGarageQueryRequestDTO;
import fr.cdrochon.smamonolithe.event.query.entities.GarageQuery;
import fr.cdrochon.smamonolithe.event.query.services.GarageQueryEventHandlerService;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.GapAwareTrackingToken;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.Headers;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Recoit les requetes de l'exterieur , par ex UI ou autre MS
 * <p>
 * La CommandGateway vient du fw Axon
 */
@RestController
//@Profile("gui")
@RequestMapping("/command/garagequery")
public class GarageQueryCommandController {
    
    // injection de la command de Axon
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final EventStore eventStore;
    
    private final GarageQueryCommandService garageQueryCommandService;
    
    public GarageQueryCommandController(CommandGateway commandGateway, QueryGateway queryGateway, EventStore eventStore,
                                        GarageQueryCommandService garageQueryCommandService) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.eventStore = eventStore;
        
        this.garageQueryCommandService = garageQueryCommandService;
    }
    
    
    /**
     * Recoit les informations du dto, et renvoi un une commande avec les attributs du dto
     * <p>
     * Les attributs de la command doivcent correspondre au dto. Le controller ne fait que retourner le dto et c'est le command handler qui va se charger
     * d'executer cette commande
     * <p>
     * L'id ne peut pas etre negatif
     *
     * @param creatClientRequestDTO
     * @return
     */
    @PostMapping("/create")
    public CompletableFuture<String> createGarage(@RequestBody CreateGarageQueryRequestDTO creatClientRequestDTO) {
        //        return commandGateway.send(new GarageQueryCreateCommand(UUID.randomUUID().toString(),
        //                                                                creatClientRequestDTO.getNomClient(),
        //                                                                creatClientRequestDTO.getMailResponsable(),
        //                                                                creatClientRequestDTO.getGarageStatus(),
        //                                                                creatClientRequestDTO.getDateQuery()));
        return garageQueryCommandService.createGarage(creatClientRequestDTO);
    }
    
    //    @PostMapping("/create")
    //    public Mono<String> createClient(@RequestBody CreateGarageQueryRequestDTO creatClientRequestDTO) {
    //
    //        return Mono.fromFuture(commandGateway.send(new GarageQueryCreateCommand(UUID.randomUUID().toString(), creatClientRequestDTO.getNomClient(),
    //                                                                                creatClientRequestDTO.getMailResponsable(),
    //                                                                                creatClientRequestDTO.getGarageStatus(), creatClientRequestDTO
    //                                                                                .getDateQuery())));
    //
    //    }
    
    //    @PostMapping("/create")
    //    public Mono<String> createClient(@RequestBody CreateGarageQueryRequestDTO creatClientRequestDTO) {
    //
    //        /* We are wrapping command into GenericCommandMessage, so we can get its identifier (correlation id) */
    //        CommandMessage<Object> command = GenericCommandMessage.asCommandMessage(new GarageQueryCreateCommand(creatClientRequestDTO.getId()));
    //
    //        /* With command identifier we can now subscribe for updates that this command produced */
    //        GarageQueryEventHandlerService query = new GarageQueryEventHandlerService(command.getIdentifier());
    //
    //        /* since we don't care about initial result, we mark it as Void.class */
    //        SubscriptionQueryResult<Void, GarageQuery> response = queryGateway.subscriptionQuery(query,
    //                                                                                             Void.class,
    //                                                                                             GarageQuery.class);
    //        return sendAndReturnUpdate(command, response)
    //                .map(GarageQuery::getIdQuery);
    //
    //    }
    //    public <U> Mono<U> sendAndReturnUpdate(Object command, SubscriptionQueryResult<?, U> result) {
    //        /* The trick here is to subscribe to initial results first, even it does not return any result
    //         Subscribing to initialResult creates a buffer for updates, even that we didn't subscribe for updates yet
    //         they will wait for us in buffer, after this we can safely send command, and then subscribe to updates */
    //        return Mono.when(result.initialResult())
    //                   .then(Mono.fromCompletionStage(() -> commandGateway.send(command)))
    //                   .thenMany(result.updates())
    //                   .timeout(Duration.ofSeconds(5))
    //                   .next()
    //                   .doFinally(unused -> result.cancel());
    //        /* dont forget to close subscription query on the end and add a timeout */
    //    }
    
    
    /**
     * Tester les events du store. On utilise l'id de l'agregat pour consulter l'etat de l'eventstore (json avec tous les events enregistrés)
     * Le format renvoyé est du json dans swagger
     *
     * @param id
     * @return
     */
    @GetMapping(path = "controlDataInEventStore/{id}") //consumes = MediaType.TEXT_EVENT_STREAM_VALUE
    public Stream readEventStore(@PathVariable String id) {
        String idd = String.valueOf(id);
        System.out.println("Valeur du segment : " + idd);
        
        return eventStore.readEvents(id).asStream();
    }
    
    /**
     * Lie un event via son id.
     *
     * @param id
     * @return
     */
    @GetMapping("readGarage/{id}")
    public CompletableFuture<Object> readGarage(@PathVariable("id") String id) {
        return queryGateway.query(new GarageQueryCreateCommand(id), ResponseTypes.instanceOf(Object.class));
    }
    
    /**
     * Pour recuperer les messages d'erreur lorsqu'une requete s'est mal passée
     *
     * @param exception
     * @return message d'erreur
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
