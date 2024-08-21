package fr.cdrochon.smamonolithe.garage.query.services;

import fr.cdrochon.smamonolithe.garage.events.GarageCreatedEvent;
import fr.cdrochon.smamonolithe.garage.query.dto.GarageQueryDTO;
import fr.cdrochon.smamonolithe.garage.query.dto.GetAllGarageDTO;
import fr.cdrochon.smamonolithe.garage.query.dto.GetGarageDTO;
import fr.cdrochon.smamonolithe.garage.query.entities.Garage;
import fr.cdrochon.smamonolithe.garage.query.mapper.GarageMapperManuel;
import fr.cdrochon.smamonolithe.garage.query.repositories.GarageRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GarageEventHandlerService {
    
    private final GarageRepository garageQueryRepository;
    
    public GarageEventHandlerService(GarageRepository garageQueryRepository) {
        this.garageQueryRepository = garageQueryRepository;
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
            log.info("Garage créé : " + garage.getNomGarage());
            
        } catch(Exception e) {
            log.error("Erreur lors de la création du garage : " + e.getMessage());
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
