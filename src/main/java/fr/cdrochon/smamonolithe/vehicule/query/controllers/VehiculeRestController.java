package fr.cdrochon.smamonolithe.vehicule.query.controllers;

import fr.cdrochon.smamonolithe.vehicule.query.mapper.VehiculeMapper;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.GetVehiculeDTO;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeResponseDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/queries")
public class VehiculeRestController {
    
    private final QueryGateway queryGateway;
    private final VehiculeRepository vehiculeRepository;
    
    public VehiculeRestController(QueryGateway queryGateway, VehiculeRepository vehiculeRepository) {
        this.queryGateway = queryGateway;
        this.vehiculeRepository = vehiculeRepository;
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
        
        GetVehiculeDTO vehiculeDTO = new GetVehiculeDTO();
        vehiculeDTO.setId(id);
        return queryGateway.query(vehiculeDTO, VehiculeResponseDTO.class).join();
    }
    
    
    /**
     * Renvoi tous les vehicules.
     * On n'utilise pas l'interface Repository usuelle, mais on créé une classe destinée à ca, qui renvoi le type de DTO sous forme de multiples instances
     *
     * @return List<VehiculeResponseDTO> comprenant l'adresse sous forme de DTO
     */
    @GetMapping("/vehicules")
    //    @PreAuthorize("hasAuthority('USER')")
    public List<VehiculeResponseDTO> getAllVehicules() {
        List<Vehicule> vehicules = vehiculeRepository.findAll();
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
    @GetMapping(value = "/vehicule/{id}/watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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
