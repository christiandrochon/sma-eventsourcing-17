package fr.cdrochon.smamonolithe.garage.query.controllers;

import fr.cdrochon.smamonolithe.garage.query.dto.*;
import fr.cdrochon.smamonolithe.garage.query.repositories.GarageRepository;
import fr.cdrochon.smamonolithe.garage.query.services.GarageEventHandlerService;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     * Renvoi les informations utiles à la partie query lors d'une recherche. Il y a moins din'ofrmations que dans l'objet renovyé pour l'affichage d'un hgarage à un concessionnaire
     * @param id
     * @return
     */
    @GetMapping(path = "/garages/{id}")
//    @PreAuthorize("hasAuthority('USER')")
    public GarageResponseDTO getGarageQuery(@PathVariable String id) {
        GetGarageDTO queryDTO = new GetGarageDTO();
        queryDTO.setId(id);
    
        return queryGateway.query(queryDTO, GarageResponseDTO.class).join();
    }
    
    /**
     * Renvoi les informations pour l'affichage dd'un garage à un concessionnaire dans thymeleaf
     * @param id
     * @return
     */
//    @GetMapping(path = "/garage/{id}")
//    //    @PreAuthorize("hasAuthority('USER')")
//    public GarageResponseRestDTO getGarageRestQuery(@PathVariable String id) {
//        GetGarageRestDTO queryDTO = new GetGarageRestDTO();
//        queryDTO.setId(id);
//        return queryGateway.query(queryDTO, GarageResponseRestDTO.class).join();
//    }
    
    /**
     * POur trouver tous les garageQueries, on n'utilise pas l'interface Repository usuelle, mais on créé une classe destinée à ca, qui renvoi le type de DTO
     * sous forme de multiples instances
     *
     * @return
     */
    @GetMapping(path = "/garages")
//    @PreAuthorize("hasAuthority('USER')")
    public List<GarageResponseDTO> getAll() {
        return queryGateway.query(new GetAllGarageDTO(), ResponseTypes.multipleInstancesOf(GarageResponseDTO.class)).join();
    }
    
    /**
     * Souscription aux events arrivant sur le bus de query ?
     *
     * @param id
     * @return
     */
//    @GetMapping(value = "/{id}/watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<GarageQueryResponseDTO> watch(@PathVariable String id) {
//
//        SubscriptionQueryResult<GarageQueryResponseDTO, GarageQueryResponseDTO> result =
//                queryGateway.subscriptionQuery(
//                        new GetGarageQueryDTO(id),
//                        ResponseTypes.instanceOf(GarageQueryResponseDTO.class),
//                        ResponseTypes.instanceOf(GarageQueryResponseDTO.class)
//                                              );
//        return result.initialResult().concatWith(result.updates());
//    }
}
