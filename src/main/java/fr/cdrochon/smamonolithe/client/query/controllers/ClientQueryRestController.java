package fr.cdrochon.smamonolithe.client.query.controllers;

import fr.cdrochon.smamonolithe.client.command.mapper.ClientMapper;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientResponseDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetClientDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/queries")
public class ClientQueryRestController {
    
    private final QueryGateway queryGateway;
    private final ClientRepository clientRepository;
    
    public ClientQueryRestController(QueryGateway queryGateway, ClientRepository clientRepository) {
        this.queryGateway = queryGateway;
        this.clientRepository = clientRepository;
    }
    
    /**
     * Renvoi les informations utiles à la partie query lors d'une recherche. Il y a moins d'informations que dans l'objet renvoyé pour l'affichage d'un
     * client à un concessionnaire
     *
     * @param id id du client
     * @return ClientResponseDTO
     */
    @GetMapping(path = "/clients/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public ClientResponseDTO getGarageQuery(@PathVariable String id) {
        GetClientDTO clientDTO = new GetClientDTO();
        clientDTO.setId(id);
        ClientResponseDTO clientResponseDTO = queryGateway.query(clientDTO, ClientResponseDTO.class).join();
        return queryGateway.query(clientDTO, ClientResponseDTO.class).join();
    }
    
    /**
     * POur trouver tous les garageQueries, on n'utilise pas l'interface Repository usuelle, mais on créé une classe destinée à ca, qui renvoi le type de DTO
     * sous forme de multiples instances
     *
     * @return List<ClientResponseDTO> comprenant l'adresse sous forme de DTO
     */
    @GetMapping(path = "/clients")
    //    @PreAuthorize("hasAuthority('USER')")
    public List<ClientResponseDTO> getAll() {
        
        List<Client> clients = clientRepository.findAll();
        List<ClientResponseDTO> clientResponseDTOS = clients.stream()
                                                            .map(ClientMapper::convertClientToClientDTO)
                                                            .collect(Collectors.toList());
        System.out.println("clientResponseDTOS = " + clientResponseDTOS);
        return clients.stream()
                      .map(ClientMapper::convertClientToClientDTO)
                      .collect(Collectors.toList());
    }
    
    
    /**
     * Renvoi un flux de GarageResponseDTO qui sera mis à jour en temps réel avec de nouvelles données chaque fois qu'un nouvel événement est publié.
     *
     * @param id id du garage
     * @return Flux de GarageResponseDTO
     */
    @GetMapping(value = "/client/{id}/watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ClientResponseDTO> watch(@PathVariable String id) {
        
        SubscriptionQueryResult<ClientResponseDTO, ClientResponseDTO> result =
                queryGateway.subscriptionQuery(
                        new GetClientDTO(id),
                        ResponseTypes.instanceOf(ClientResponseDTO.class),
                        ResponseTypes.instanceOf(ClientResponseDTO.class)
                                              );
        return result.initialResult().concatWith(result.updates());
    }
}
