package fr.cdrochon.smamonolithe.garage.query.services;

import fr.cdrochon.smamonolithe.garage.events.GarageCreatedEvent;
import fr.cdrochon.smamonolithe.garage.query.dto.GarageQueryDTO;
import fr.cdrochon.smamonolithe.garage.query.dto.GetAllGarageDTO;
import fr.cdrochon.smamonolithe.garage.query.dto.GetGarageDTO;
import fr.cdrochon.smamonolithe.garage.query.entities.Garage;
import fr.cdrochon.smamonolithe.garage.query.events.GarageCreatedApplicationEvent;
import fr.cdrochon.smamonolithe.garage.query.mapper.GarageMapperManuel;
import fr.cdrochon.smamonolithe.garage.query.repositories.GarageRepository;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GarageEventHandlerService {
    
    private final GarageRepository garageQueryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public GarageEventHandlerService(GarageRepository garageQueryRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.garageQueryRepository = garageQueryRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    /**
     * Souscrit à l'événement GarageCreatedEvent sur le bus d'évènement pour sauvegarder le garage dans la base de données
     *
     * @param event événement de création d'un garage
     */
    @EventHandler
    @Transactional
    public void on(GarageCreatedEvent event) {
        
        try {
            Garage garage = new Garage();
            garage.setIdQuery(event.getId());
            garage.setNomGarage(event.getNomGarage());
            garage.setMailResponsable(event.getMailResponsable());
            garage.setAdresseGarage(event.getAdresseGarage());
            garage.setGarageStatus(event.getGarageStatus());
            
            garageQueryRepository.save(garage);
            BusinessLoggers.business().info("BIZ_GARAGE_CREATED garageId={} nomGarage={} status={}",
                                            garage.getIdQuery(),
                                            garage.getNomGarage(),
                                            garage.getGarageStatus());
            
            // Publier un événement Spring APRÈS la transaction pour notifier les listeners (notamment GarageCommandService)
            final GarageQueryDTO garageDTO = GarageMapperManuel.convertGarageToGarageDTO(garage);
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        applicationEventPublisher.publishEvent(new GarageCreatedApplicationEvent(this, garageDTO));
                    }
                });
            } else {
                // Si pas de synchronisation active, publier immédiatement
                applicationEventPublisher.publishEvent(new GarageCreatedApplicationEvent(this, garageDTO));
            }

        } catch(Exception e) {
            log.error("TECH_GARAGE_PERSIST_ERROR garageId={} message={}", event.getId(), e.getMessage(), e);
            throw new IllegalArgumentException("Erreur lors de la création du garage : " + e.getMessage());
        }
    }

    /**
     * Recupere un garage avec son id
     *
     * @param getGarageQueryDTO DTO contenant l'id du garage à recuperer
     * @return GarageResponseDTO
     */
    @QueryHandler
    public GarageQueryDTO on(GetGarageDTO getGarageQueryDTO) {
        return garageQueryRepository.findById(getGarageQueryDTO.getId())
                                    .map(GarageMapperManuel::convertGarageToGarageDTO)
                                    .orElseThrow(() -> new IllegalArgumentException("Garage non trouvé"));
    }
    
    /**
     * Recupere tous les garages
     *
     * @return List<GarageResponseDTO>
     */
    @QueryHandler
    public List<GarageQueryDTO> on(GetAllGarageDTO getAllGarageQueries) {
        List<Garage> garageQueries = garageQueryRepository.findAll();
        return garageQueries.stream().map(GarageMapperManuel::convertGarageToGarageDTO).collect(Collectors.toList());
    }
    
}
