package fr.cdrochon.smamonolithe.event.query.controllers;

import fr.cdrochon.smamonolithe.event.query.dto.GarageQueryResponseDTO;
import fr.cdrochon.smamonolithe.event.query.dto.GetGarageQueryDTO;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/query")
public class GarageQueryRestController {
    
    private QueryGateway queryGateway;
    
    public GarageQueryRestController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }
    
    @GetMapping("/garagequery/{id}")
    public GarageQueryResponseDTO getGarageQuery(@PathVariable String id){
        GetGarageQueryDTO queryDTO = new GetGarageQueryDTO();
        queryDTO.setId(id);
        return queryGateway.query(queryDTO, GarageQueryResponseDTO.class).join();
    }
    
    /**
     * Souscription aux events arrivant sur le bus de query ?
     * @param accountId
     * @return
     */
    @GetMapping(value = "/{garageId}/watch",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<GarageQueryResponseDTO> watch(@PathVariable String accountId){
        
        SubscriptionQueryResult<GarageQueryResponseDTO, GarageQueryResponseDTO> result=
                queryGateway.subscriptionQuery(
                        new GetGarageQueryDTO(accountId),
                        ResponseTypes.instanceOf(GarageQueryResponseDTO.class),
                        ResponseTypes.instanceOf(GarageQueryResponseDTO.class)
                                              );
        return result.initialResult().concatWith(result.updates());
    }
}
