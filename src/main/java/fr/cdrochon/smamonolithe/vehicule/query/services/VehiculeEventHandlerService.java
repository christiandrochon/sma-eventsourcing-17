package fr.cdrochon.smamonolithe.vehicule.query.services;

import fr.cdrochon.smamonolithe.client.events.ClientCreatedEvent;
import fr.cdrochon.smamonolithe.vehicule.event.VehiculeCreatedEvent;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.GetAllVehiculesDTO;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.GetImmatDTO;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.GetVehiculeDTO;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.query.mapper.VehiculeMapper;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class VehiculeEventHandlerService {
    
    private final VehiculeRepository vehiculeRepository;
    
    public VehiculeEventHandlerService(VehiculeRepository vehiculeRepository) {
        this.vehiculeRepository = vehiculeRepository;
    }
    
    /**
     * On fait un subscribe avec @EventHandler = j'ecoute ce que fait le service VehiculeCreatedEvent
     * <p>
     * EventMessage sert à recuperer toutes les informations sur l'event avec la methode payLoad(). Il est donc plus general que l'event créé par moi-meme
     *
     * @param event l'event GarageQueryCreatedEvent
     */
    @EventHandler
    public void on(VehiculeCreatedEvent event, EventMessage<ClientCreatedEvent> eventMessage) {
        log.info("********************************");
        log.info("ClientQueryCreatedEvent received !!!!!!!!!!!!!!!!!!!!!!");
        log.info("Identifiant d'evenement : " + event.getId());
        log.info("Identifiant d'agregat : " + eventMessage.getIdentifier());
        
        try {
            Vehicule vehicule = new Vehicule();
            vehicule.setIdVehicule(event.getId());
            vehicule.setImmatriculationVehicule(event.getImmatriculationVehicule());
            vehicule.setDateMiseEnCirculationVehicule(event.getDateMiseEnCirculationVehicule());
            vehicule.setVehiculeStatus(event.getVehiculeStatus());
            vehiculeRepository.save(vehicule);
        } catch(Exception e) {
            System.out.println("EXTRACTTTTTTTTTTTTTTTTTTTTTT VEHICULE ERRRRRRRRRRRRRRRRRRRROOR : " + e.getMessage());
        }
    }
    
    /**
     * Recupere et renvoi un vehicule avec son id
     *
     * @param getVehiculeDTO id du vehicule
     * @return VehiculeResponseDTO contenant les informations du vehicule
     */
    @QueryHandler
    public VehiculeQueryDTO on(GetVehiculeDTO getVehiculeDTO) {
        return vehiculeRepository.findById(getVehiculeDTO.getId())
                                 .map(VehiculeMapper::convertVehiculeToVehiculeDTO)
                                 .orElseThrow(() -> new EntityNotFoundException("Vehicule non trouvé !"));
    }
    
    /**
     * Recupere et renvoi un vehicule avec son immatriculation
     *
     * @param getImmatDTO DTO contenant l'immatriculation du vehicule
     * @return VehiculeResponseDTO contenant les informations du vehicule
     */
    @QueryHandler
    public VehiculeQueryDTO on(GetImmatDTO getImmatDTO) {
        Vehicule vehicule = vehiculeRepository.findByImmatriculationVehicule(getImmatDTO.getImmatriculation());
        if(vehicule == null) {
            throw new EntityNotFoundException("Vehicule non trouvé !");
        }
        VehiculeQueryDTO vehiculeResponseDTO = VehiculeMapper.convertVehiculeToVehiculeDTO(vehicule);
        return vehiculeResponseDTO;
    }
    
    /**
     * Recupere et renvoi tous les vehicules
     *
     * @return List<VehiculeResponseDTO> contenant les informations de tous les vehicules
     */
    @QueryHandler
    public List<VehiculeQueryDTO> on(GetAllVehiculesDTO getAllVehiculesDTO) {
        List<Vehicule> vehicules = vehiculeRepository.findAll();
        return vehicules.stream().map(VehiculeMapper::convertVehiculeToVehiculeDTO).collect(Collectors.toList());
    }
}
