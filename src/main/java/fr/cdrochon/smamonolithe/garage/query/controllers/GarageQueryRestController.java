package fr.cdrochon.smamonolithe.garage.query.controllers;

import fr.cdrochon.smamonolithe.garage.query.mapper.GarageMapperManuel;
import fr.cdrochon.smamonolithe.garage.query.dto.GarageResponseDTO;
import fr.cdrochon.smamonolithe.garage.query.dto.GetGarageDTO;
import fr.cdrochon.smamonolithe.garage.query.entities.Garage;
import fr.cdrochon.smamonolithe.garage.query.repositories.GarageRepository;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/queries")
//@PreAuthorize("hasAuthority('USER')")
public class GarageQueryRestController {
    
    private final QueryGateway queryGateway;
    private final GarageRepository garageRepository;
    
    
    public GarageQueryRestController(QueryGateway queryGateway, GarageRepository garageRepository) {
        this.queryGateway = queryGateway;
        this.garageRepository = garageRepository;
    }
    
    /**
     * Renvoi les informations utiles à la partie query lors d'une recherche. Il y a moins din'ofrmations que dans l'objet renovyé pour l'affichage d'un
     * hgarage à un concessionnaire
     *
     * @param id id du garage
     * @return GarageResponseDTO
     */
    @GetMapping(path = "/garages/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public GarageResponseDTO getGarageQuery(@PathVariable String id) {
        GetGarageDTO queryDTO = new GetGarageDTO();
        queryDTO.setId(id);
        
        return queryGateway.query(queryDTO, GarageResponseDTO.class).join();
    }
    
    /**
     * POur trouver tous les garageQueries, on n'utilise pas l'interface Repository usuelle, mais on créé une classe destinée à ca, qui renvoi le type de DTO
     * sous forme de multiples instances
     *
     * @return List<GarageResponseDTO> comprenant l'adresse sous forme de DTO
     */
    @GetMapping(path = "/garages")
    //    @PreAuthorize("hasAuthority('USER')")
    public List<GarageResponseDTO> getAll() {
        
        List<Garage> garages = garageRepository.findAll();
        return garages.stream()
                      .map(GarageMapperManuel::convertGarageToGarageDTO)
                      .collect(Collectors.toList());
    }
    
    
    /**
     * Renvoi un flux de GarageResponseDTO qui sera mis à jour en temps réel avec de nouvelles données chaque fois qu'un nouvel événement est publié.
     *
     * @param id id du garage
     * @return Flux de GarageResponseDTO
     */
//    @GetMapping(value = "/garage/{id}/watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<GarageResponseDTO> watch(@PathVariable String id) {
//
//        try(SubscriptionQueryResult<GarageResponseDTO, GarageResponseDTO> result = queryGateway.subscriptionQuery(
//                new GetGarageDTO(id),
//                ResponseTypes.instanceOf(GarageResponseDTO.class),
//                ResponseTypes.instanceOf(GarageResponseDTO.class)
//                                                                                                                 )) {
//            return result.initialResult().concatWith(result.updates());
//        }
//    }
}
