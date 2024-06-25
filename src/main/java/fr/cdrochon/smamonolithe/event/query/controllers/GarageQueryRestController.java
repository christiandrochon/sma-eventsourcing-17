package fr.cdrochon.smamonolithe.event.query.controllers;

import fr.cdrochon.smamonolithe.event.query.dto.GarageQueryResponseDTO;
import fr.cdrochon.smamonolithe.event.query.dto.GetGarageQueryDTO;
import fr.cdrochon.smamonolithe.event.query.dto.GetAllGarageQueriesDTO;
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
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(path = "/query")
public class GarageQueryRestController {
    
    private final QueryGateway queryGateway;
    
    public GarageQueryRestController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }
    
    @GetMapping(path = "/garages/{id}")
    public GarageQueryResponseDTO getGarageQuery(@PathVariable String id) {
        GetGarageQueryDTO queryDTO = new GetGarageQueryDTO();
        queryDTO.setId(id);
        return queryGateway.query(queryDTO, GarageQueryResponseDTO.class).join();
    }
    
    /**
     * POur trouver tous les garageQueries, on n'utilise pas l'interface Repository usuelle, mais on créé une classe destinée à ca, qui renvoi le type de DTO
     * sous forme de multiples instances
     *
     * @return
     */
    @GetMapping(path = "/garages")
    public List<GarageQueryResponseDTO> getAll() {
        return queryGateway.query(new GetAllGarageQueriesDTO(), ResponseTypes.multipleInstancesOf(GarageQueryResponseDTO.class)).join();
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
