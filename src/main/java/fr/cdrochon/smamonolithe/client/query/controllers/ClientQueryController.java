package fr.cdrochon.smamonolithe.client.query.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetClientDTO;
import fr.cdrochon.smamonolithe.client.query.mapper.ClientQueryMapper;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import fr.cdrochon.smamonolithe.json.Views;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/queries")
@Slf4j
public class ClientQueryController {
    
    private final QueryGateway queryGateway;
    private final ClientRepository clientRepository;
    
    public ClientQueryController(QueryGateway queryGateway, ClientRepository clientRepository) {
        this.queryGateway = queryGateway;
        this.clientRepository = clientRepository;
    }
    
    //FIXME: 2021-08-25 - CDROCHON - A REVOIR -> VALIDATION DU FORMULAIRE PAS TERRIBLE au niveau des animations et des messages d'erreurs
    
    /**
     * Méthode asynchrone qui renvoi un client dto.
     *
     * @param id id du client
     * @return Mono de ClientResponseDTO
     */
    @GetMapping(path = "/clients/{id}")
    @JsonView(Views.ClientView.class)
    public Mono<ClientQueryDTO> getClientByIdAsync(@PathVariable String id) {
        CompletableFuture<ClientQueryDTO> future =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return clientRepository.findById(id)
                                               .map(ClientQueryMapper::convertClientToClientDTO)
                                               .orElseThrow(() -> new RuntimeException("Client not found"));
                    } catch(Exception e) {
                        log.error("Error retrieving client with id {}: {}", id, e.getMessage(), e);
                        throw new RuntimeException("Error retrieving client", e);
                    }
                });
        Mono<ClientQueryDTO> mono = Mono.fromFuture(future);
        return mono;
    }
    
    /**
     * Retourne la liste de tous les clients de manière asynchrone
     *
     * @return Flux de ClientResponseDTO
     */
    @GetMapping(path = "/clients")
    @JsonView(Views.ClientView.class)
    public Flux<ClientQueryDTO> getClientsAsync() {
        CompletableFuture<List<ClientQueryDTO>> future = CompletableFuture.supplyAsync(() -> {
            List<ClientQueryDTO> clients =
                    clientRepository.findAll()
                                    .stream()
                                    .map(ClientQueryMapper::convertClientToClientDTO)
                                    .collect(Collectors.toList());
            return clients;
        });
        Flux<ClientQueryDTO> flux = Flux.fromStream(future.join().stream());
        return flux;
    }
    
    
    /**
     * Renvoi un flux de GarageResponseDTO qui sera mis à jour en temps réel avec de nouvelles données chaque fois qu'un nouvel événement est publié.
     *
     * @param id id du garage
     * @return Flux de GarageResponseDTO
     */
    @GetMapping(value = "/client/{id}/watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ClientQueryDTO> watch(@PathVariable String id) {
        
        try(SubscriptionQueryResult<ClientQueryDTO, ClientQueryDTO> result = queryGateway.subscriptionQuery(
                new GetClientDTO(id),
                ResponseTypes.instanceOf(ClientQueryDTO.class),
                ResponseTypes.instanceOf(ClientQueryDTO.class)
                                                                                                           )) {
            return result.initialResult().concatWith(result.updates());
        }
    }
}
