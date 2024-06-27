package fr.cdrochon.smamonolithe.garage.query.services;

import fr.cdrochon.smamonolithe.garage.command.mapper.GarageMapper;
import fr.cdrochon.smamonolithe.garage.command.mapper.GarageMapperManuel;
import fr.cdrochon.smamonolithe.garage.events.GarageCreatedEvent;
import fr.cdrochon.smamonolithe.garage.query.dto.GarageResponseDTO;
import fr.cdrochon.smamonolithe.garage.query.dto.GetGarageDTO;
import fr.cdrochon.smamonolithe.garage.query.entities.Garage;
import fr.cdrochon.smamonolithe.garage.query.dto.GetAllGarageDTO;
import fr.cdrochon.smamonolithe.garage.query.repositories.GarageRepository;
import fr.cdrochon.smamonolithe.garage.query.repositories.GarageTransactionRepository;
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
public class GarageEventHandlerService {
    private final GarageRepository garageQueryRepository;
    private final GarageTransactionRepository garageQueryTransactionRepository;
    
    private final GarageMapper garageQueryMapper;
    private final GarageMapperManuel garageMapperManuel;

    private final QueryUpdateEmitter queryUpdateEmitter;

    
    //    private Instant lastUpdate;
    //    private String identifier;
    
    public GarageEventHandlerService(GarageRepository garageQueryRepository, GarageTransactionRepository garageQueryTransactionRepository,
                                     QueryUpdateEmitter queryUpdateEmitter, GarageMapper garageQueryMapper, GarageMapperManuel garageMapperManuel) {
        this.garageQueryRepository = garageQueryRepository;
        this.garageQueryTransactionRepository = garageQueryTransactionRepository;
        this.queryUpdateEmitter = queryUpdateEmitter;
        this.garageQueryMapper = garageQueryMapper;
        this.garageMapperManuel = garageMapperManuel;
    }
    
    /**
     * On fait un subscribe avec @EventHandler = j'ecoute ce que fait le service GarageQueryCreatedEvent
     * <p>
     * EventMessage sert à recuperer toutes les informations sur l'event avec la methode payLoad(). Il est donc plus general que l'event créé par moi-meme
     *
     * @param event
     */
    @EventHandler
    public void on(GarageCreatedEvent event, EventMessage<GarageCreatedEvent> eventMessage) {
        log.info("********************************");
        log.info("GarageQueryCreatedEvent received !!!!!!!!!!!!!!!!!!!!!!");
        //        log.info("Identifiant d'evenement : " + messageId + " : " + eventMessage.getIdentifier());
        log.info("Identifiant d'agregat : " + event.getId());
        
        try {
            Garage garageQuery = new Garage();
            garageQuery.setIdQuery(event.getId());
            garageQuery.setNomGarage(event.getNomGarage());
            garageQuery.setMailResponsable(event.getMailResponsable());
            garageQuery.setAdresseGarage(event.getAdresseGarage());
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
    public GarageResponseDTO on(GetGarageDTO getGarageQueryDTO) {
        Garage garageQuery = garageQueryRepository.findById(getGarageQueryDTO.getId()).get();
        
//        return garageQueryMapper.garageQueryToGarageQueryDTO(garageQuery);
        return garageQueryRepository.findById(getGarageQueryDTO.getId()).map(GarageMapperManuel::toGarageDTO).get();
    }
    
    /**
     * Recupere tous les garages
     *
     * @param
     * @return
     */
    @QueryHandler
    public List<GarageResponseDTO> on(GetAllGarageDTO getAllGarageQueries) {
        List<Garage> garageQueries = garageQueryRepository.findAll();
        return garageQueries.stream().map(garageQueryMapper::garageQueryToGarageQueryDTO).collect(Collectors.toList());
    }

}
