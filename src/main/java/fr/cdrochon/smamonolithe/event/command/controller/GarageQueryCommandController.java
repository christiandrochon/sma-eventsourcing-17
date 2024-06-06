package fr.cdrochon.smamonolithe.event.command.controller;

import fr.cdrochon.smamonolithe.event.commonapi.command.GarageQueryCreateCommand;
import fr.cdrochon.smamonolithe.event.commonapi.dto.CreateGarageQueryRequestDTO;
import fr.cdrochon.smamonolithe.event.query.services.GarageQueryEventHandlerService;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.GapAwareTrackingToken;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
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
@Profile("gui")
@RequestMapping("/command/garagequery")
public class GarageQueryCommandController {
    
    // injection de la command de Axon
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final EventStore eventStore;
    
    public GarageQueryCommandController(CommandGateway commandGateway, QueryGateway queryGateway, EventStore eventStore) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.eventStore = eventStore;
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
    public CompletableFuture<Object> createClient(@RequestBody CreateGarageQueryRequestDTO creatClientRequestDTO) {
//        CommandMessage<Object> commandMessage = GenericCommandMessage.asCommandMessage(creatClientRequestDTO);
        //TrackingToken trackingToken = createToken(eventStore.readEvents.asStream().map(DomainEventMessage::getSequenceNumber).toList());
        
        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));

        return commandGateway.send(new GarageQueryCreateCommand(UUID.randomUUID().toString(), creatClientRequestDTO.getNomClient(),
                                                                creatClientRequestDTO.getMailResponsable()));
    }
//    @PostMapping("/create")
//    public Mono<Object> createClient(@RequestBody CreateGarageQueryRequestDTO creatClientRequestDTO) {
//        CommandMessage<Object> commandMessage = GenericCommandMessage.asCommandMessage(creatClientRequestDTO);
//        //TrackingToken trackingToken = createToken(eventStore.readEvents.asStream().map(DomainEventMessage::getSequenceNumber).toList());
//
//
////        return Mono.fromFuture(commandGateway.send(new GarageQueryCreateCommand(new Random().nextLong(0, 10000000000000000L), creatClientRequestDTO.getNomClient(),
////                                                                creatClientRequestDTO.getMailResponsable())));
//        return Mono.fromFuture(commandGateway.send(new GarageQueryCreateCommand(UUID.randomUUID().toString(), creatClientRequestDTO.getNomClient(),
//                                                                                creatClientRequestDTO.getMailResponsable())));
//
//    }
    
    /**
     * Tester les events du store. On utilise l'id de l'agregat pour consulter l'etat de l'eventstore (json avec tous les events enregistrés)
     *
     * @param id
     * @return
     */
    @GetMapping(path = "eventStore/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Stream readEventStore(@PathVariable String id) {
        String idd = String.valueOf(id);
        System.out.println(idd);

        
        return eventStore.readEvents(id).asStream();
    }
    
//    @GetMapping("readGarage/{id}")
//    public CompletableFuture<Object> readGarage(@PathVariable("id") String id){
//        return queryGateway.query(new GarageQueryCreateCommand(id), ResponseTypes.instanceOf(Object.class));
//    }
}
