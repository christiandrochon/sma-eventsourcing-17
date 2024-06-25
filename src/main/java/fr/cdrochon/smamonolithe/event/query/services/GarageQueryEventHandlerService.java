package fr.cdrochon.smamonolithe.event.query.services;

import fr.cdrochon.smamonolithe.event.commonapi.events.GarageQueryCreatedEvent;
import fr.cdrochon.smamonolithe.event.mapper.GarageQueryMapper;
import fr.cdrochon.smamonolithe.event.query.dto.GarageQueryResponseDTO;
import fr.cdrochon.smamonolithe.event.query.dto.GetGarageQueryDTO;
import fr.cdrochon.smamonolithe.event.query.entities.GarageQuery;
import fr.cdrochon.smamonolithe.event.query.repositories.GarageQueryRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Service;

@Service
//@Transactional
@Slf4j
//@Profile("query")
//@ProcessingGroup("garagequery-summary")
public class GarageQueryEventHandlerService {
    private final GarageQueryRepository garageQueryRepository;
//    private final EventStore eventStore;
//    private final QueryGateway queryGateway;
    private final GarageQueryMapper garageQueryMapper;
//    private final Map<String, GarageSummary> garageSummaryReadModel;
    private final QueryUpdateEmitter queryUpdateEmitter;
    private final EventSourcingServiceImpl eventSourcingServiceImpl;
    
    //    private Instant lastUpdate;
//    private String identifier;
    
    public GarageQueryEventHandlerService(GarageQueryRepository garageQueryRepository, QueryUpdateEmitter queryUpdateEmitter,
                                          GarageQueryMapper garageQueryMapper, EventSourcingServiceImpl eventSourcingServiceImpl) {
        this.garageQueryRepository = garageQueryRepository;
        this.queryUpdateEmitter = queryUpdateEmitter;
        this.garageQueryMapper = garageQueryMapper;
        this.eventSourcingServiceImpl = eventSourcingServiceImpl;
    }
    
    
    //    public GarageQueryEventHandlerService(GarageQueryRepository garageQueryRepository, EventStore eventStore, QueryGateway queryGateway,
//                                          QueryUpdateEmitter queryUpdateEmitter) {
//        this.garageQueryRepository = garageQueryRepository;
//        this.eventStore = eventStore;
//        this.queryGateway = queryGateway;
//
//        this.garageSummaryReadModel = new ConcurrentHashMap<>();
//        this.queryUpdateEmitter = queryUpdateEmitter;
//    }
    
    //    public GarageQueryEventHandlerService(String identifier) {
    //        this.identifier = identifier;
    //    }
    //
    //    public GarageQueryEventHandlerService() {
    //    }
    
    /**
     * On fait un subscribe = j''ecoute ce que fait le service GarageQueryCreatedEvent
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
        
        //        GarageQuery garage = new GarageQuery(event.id(),
        //                                             eventMessage.getTimestamp(),
        //                                             eventMessage.getPayload().nomClient(),
        //                                             event.mailResp(),
        //                                             GarageStatus.CREATED);
        try {
            GarageQuery garageQuery = new GarageQuery();
            garageQuery.setIdQuery(event.getId());
            garageQuery.setNomGarage(event.getNomGarage());
            garageQuery.setMailResponsable(event.getMailResponsable());
            garageQueryRepository.save(garageQuery);
        } catch(Exception e){
            System.out.println("ERRRRRRRRRRRRRRRRRRRROOR : " + e.getMessage());
            e.getMessage();
        }
//        try {
//
//
//            GarageQuery garage = new GarageQuery();
////            garage.setIdQuery(eventMessage.getIdentifier());
//            garage.setIdQuery(event.getId());
////            garage.setIdQuery(garageQueryIdinteger);
//            //        garage.setNomGarage(event.getNomClient());
//            garage.setNomGarage(eventMessage.getPayload().getNomGarage());
//            garage.setMailResponsable(event.getMailResponsable());
////            garage.setGarageStatus(GarageStatus.CREATED);
////            garage.setDateQuery(eventMessage.getTimestamp());
//            garageQueryRepository.save(garage);
//        } catch(Exception e) {
//            System.out.println("EROOOOOOOOOOOOOOOOOOOOOOOOOOOOR : " + e.getMessage());
//            e.getMessage();
//        }
        
        
    }
    @QueryHandler
    public GarageQueryResponseDTO on(GetGarageQueryDTO getGarageQueryDTO){
        GarageQuery garageQuery = garageQueryRepository.findById(getGarageQueryDTO.getId()).get();
        return garageQueryMapper.garageQueryToGarageQueryDTO(garageQuery);
    }
    
    //    @EventHandler
    //    public void on(GarageQueryCreatedEvent event, @Timestamp Instant timestamp) {
    //        lastUpdate = timestamp;
    //        /*
    //         * Update our read model by inserting the new card. This is done so that upcoming regular
    //         * (non-subscription) queries get correct data.
    //         */
    //        GarageSummary garageSummary = GarageSummary.create(event.id(), event.nomClient(), event.mailResp(), event.garageStatus());
    //        garageSummaryReadModel.put(event.id(), garageSummary);
    //
    //
    //    }
    
    
}
