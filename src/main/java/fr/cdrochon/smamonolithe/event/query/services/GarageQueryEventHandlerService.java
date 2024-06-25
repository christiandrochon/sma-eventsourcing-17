package fr.cdrochon.smamonolithe.event.query.services;

import fr.cdrochon.smamonolithe.event.commonapi.events.GarageQueryCreatedEvent;
import fr.cdrochon.smamonolithe.event.mapper.GarageQueryMapper;
import fr.cdrochon.smamonolithe.event.query.dto.GarageQueryResponseDTO;
import fr.cdrochon.smamonolithe.event.query.dto.GetGarageQueryDTO;
import fr.cdrochon.smamonolithe.event.query.entities.GarageQuery;
import fr.cdrochon.smamonolithe.event.query.dto.GetAllGarageQueriesDTO;
import fr.cdrochon.smamonolithe.event.query.repositories.GarageQueryRepository;
import fr.cdrochon.smamonolithe.event.query.repositories.GarageQueryTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class GarageQueryEventHandlerService {
    private final GarageQueryRepository garageQueryRepository;
    private final GarageQueryTransactionRepository garageQueryTransactionRepository;
    
    private final GarageQueryMapper garageQueryMapper;

    private final QueryUpdateEmitter queryUpdateEmitter;

    
    //    private Instant lastUpdate;
    //    private String identifier;
    
    public GarageQueryEventHandlerService(GarageQueryRepository garageQueryRepository, GarageQueryTransactionRepository garageQueryTransactionRepository,
                                          QueryUpdateEmitter queryUpdateEmitter, GarageQueryMapper garageQueryMapper) {
        this.garageQueryRepository = garageQueryRepository;
        this.garageQueryTransactionRepository = garageQueryTransactionRepository;
        this.queryUpdateEmitter = queryUpdateEmitter;
        this.garageQueryMapper = garageQueryMapper;
    }
    
    /**
     * On fait un subscribe avec @EventHandler = j'ecoute ce que fait le service GarageQueryCreatedEvent
     * <p>
     * EventMessage sert à recuperer toutes les informations sur l'event avec la methode payLoad(). Il est donc plus general que l'event créé par moi-meme
     *
     * @param event
     */
    @EventHandler
    public void on(GarageQueryCreatedEvent event, EventMessage<GarageQueryCreatedEvent> eventMessage) {
        log.info("********************************");
        log.info("GarageQueryCreatedEvent received !!!!!!!!!!!!!!!!!!!!!!");
        //        log.info("Identifiant d'evenement : " + messageId + " : " + eventMessage.getIdentifier());
        log.info("Identifiant d'agregat : " + event.getId());
        
        try {
            GarageQuery garageQuery = new GarageQuery();
            garageQuery.setIdQuery(event.getId());
            garageQuery.setNomGarage(event.getNomGarage());
            garageQuery.setMailResponsable(event.getMailResponsable());
            garageQuery.setGarageStatus(event.getClientStatus());
            
            garageQueryRepository.save(garageQuery);
        } catch(Exception e) {
            System.out.println("ERRRRRRRRRRRRRRRRRRRROOR : " + e.getMessage());
            e.getMessage();
        }
    }
    
    /**
     * Recupere un garage avec son id
     *
     * @param
     * @return
     */
    @QueryHandler
    public GarageQueryResponseDTO on(GetGarageQueryDTO getGarageQueryDTO) {
        GarageQuery garageQuery = garageQueryRepository.findById(getGarageQueryDTO.getId()).get();
        return garageQueryMapper.garageQueryToGarageQueryDTO(garageQuery);
    }
    
    /**
     * Recupere tous les garages
     *
     * @param
     * @return
     */
    @QueryHandler
    public List<GarageQueryResponseDTO> on(GetAllGarageQueriesDTO getAllGarageQueries) {
        List<GarageQuery> garageQueries = garageQueryRepository.findAll();
        return garageQueries.stream().map(garageQueryMapper::garageQueryToGarageQueryDTO).collect(Collectors.toList());
    }

}
