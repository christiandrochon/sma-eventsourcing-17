package fr.cdrochon.smamonolithe.vehicule.query.controllers;

import fr.cdrochon.smamonolithe.client.command.mapper.ClientMapper;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientResponseDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetClientDTO;
import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import fr.cdrochon.smamonolithe.vehicule.command.mapper.VehiculeMapper;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.GetVehiculeDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.VehiculeResponseDTO;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
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
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/queries")
public class VehiculeRestController {
    
    private final QueryGateway queryGateway;
    private final VehiculeRepository vehiculeRepository;
    private final ClientRepository clientRepository;
    
    public VehiculeRestController(QueryGateway queryGateway, VehiculeRepository vehiculeRepository, ClientRepository clientRepository) {
        this.queryGateway = queryGateway;
        this.vehiculeRepository = vehiculeRepository;
        this.clientRepository = clientRepository;
    }
    
    /**
     * Renvoi les informations considérées comme utiles à la partie query lors de la recherche d'un vehicule par son id.
     *
     * @param id id du vehicule
     * @return VehiculeResponseDTO
     */
    @GetMapping("/vehicules/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    //    @CircuitBreaker(name = "clientService", fallbackMethod = "getDefaultClient")
    public VehiculeResponseDTO getVehicule(@PathVariable String id) {
        
        VehiculeResponseDTO vehiculeResponseDTO = vehiculeRepository.findById(id).get();
        GetVehiculeDTO vehiculeDTO = new GetVehiculeDTO();
        vehiculeDTO.setId(id);
        vehiculeResponseDTO = queryGateway.query(vehiculeDTO, VehiculeResponseDTO.class).join();
        return vehiculeResponseDTO;
    }
    
    
    /**
     * Renvoi tous les vehicule.
     * On n'utilise pas l'interface Repository usuelle, mais on créé une classe destinée à ca, qui renvoi le type de DTO sous forme de multiples instances
     *
     * @return List<VehiculeResponseDTO> comprenant l'adresse sous forme de DTO
     */
    @GetMapping("/vehicules")
    //    @PreAuthorize("hasAuthority('USER')")
    public List<VehiculeResponseDTO> getAllVehicules() {
        List<VehiculeResponseDTO> vehicules = vehiculeRepository.findAll();
        
        List<VehiculeResponseDTO> vehiculeResponseDTOS = vehicules.stream()
                                                                  .map(VehiculeMapper::convertVehiculeToVehiculeDTO)
                                                                  .collect(Collectors.toList());
        return vehicules.stream()
                        .map(VehiculeMapper::convertVehiculeToVehiculeDTO)
                        .collect(Collectors.toList());
    }
    
    /**
     * Renvoi un flux de vEHICULEResponseDTO qui sera mis à jour en temps réel avec de nouvelles données chaque fois qu'un nouvel événement est publié.
     *
     * @param id id du vehicule
     * @return Flux de VehiculeResponseDTO
     */
    @GetMapping(value = "/client/{id}/watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VehiculeResponseDTO> watch(@PathVariable String id) {
        
        try(SubscriptionQueryResult<VehiculeResponseDTO, VehiculeResponseDTO> result = queryGateway.subscriptionQuery(
                new GetVehiculeDTO(id),
                ResponseTypes.instanceOf(VehiculeResponseDTO.class),
                ResponseTypes.instanceOf(VehiculeResponseDTO.class)
                                                                                                                     )) {
            return result.initialResult().concatWith(result.updates());
        }
    }
}
