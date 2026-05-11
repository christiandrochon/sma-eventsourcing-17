package fr.cdrochon.smamonolithe.vehicule.query.services;

import fr.cdrochon.smamonolithe.vehicule.event.VehiculeCreatedEvent;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.GetAllVehiculesDTO;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.GetImmatDTO;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.GetVehiculeDTO;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.query.events.VehiculeCreatedApplicationEvent;
import fr.cdrochon.smamonolithe.vehicule.query.mapper.VehiculeQueryMapper;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hibernate.TransactionException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VehiculeEventHandlerService {
    
    private final VehiculeRepository vehiculeRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public VehiculeEventHandlerService(VehiculeRepository vehiculeRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.vehiculeRepository = vehiculeRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    /**
     * On fait un subscribe avec @EventHandler sur le service VehiculeCreatedEvent
     *
     * @param event l'event VehiculeCreatedEvent est declenché lors de la creation d'un document
     */
    @EventHandler
    @Transactional
    public void on(VehiculeCreatedEvent event) {
        try {
            Vehicule vehicule = new Vehicule();
            vehicule.setId(event.getId());
            vehicule.setImmatriculationVehicule(event.getImmatriculationVehicule());
            vehicule.setDateMiseEnCirculationVehicule(event.getDateMiseEnCirculationVehicule());
            vehicule.setVehiculeStatus(event.getVehiculeStatus());
            vehiculeRepository.save(vehicule);
            BusinessLoggers.business().info("BIZ_VEHICULE_CREATED vehiculeId={} immatriculation={} status={}",
                                            vehicule.getId(),
                                            vehicule.getImmatriculationVehicule(),
                                            vehicule.getVehiculeStatus());
            
            // Publier un événement Spring APRÈS la transaction pour notifier les listeners (notamment VehiculeCommandService)
            final VehiculeQueryDTO vehiculeDTO = VehiculeQueryMapper.convertVehiculeToVehiculeDTO(vehicule);
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        applicationEventPublisher.publishEvent(new VehiculeCreatedApplicationEvent(this, vehiculeDTO));
                    }
                });
            } else {
                // Si pas de synchronisation active, publier immédiatement
                applicationEventPublisher.publishEvent(new VehiculeCreatedApplicationEvent(this, vehiculeDTO));
            }
        } catch(Exception e) {
            log.error("TECH_VEHICULE_PERSIST_ERROR vehiculeId={} message={}", event.getId(), e.getMessage(), e);
            throw new TransactionException("Erreur lors de la sauvegarde du vehicule");
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
                                 .map(VehiculeQueryMapper::convertVehiculeToVehiculeDTO)
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
        return VehiculeQueryMapper.convertVehiculeToVehiculeDTO(vehicule);
    }
    
    /**
     * Recupere et renvoi tous les vehicules
     *
     * @return List<VehiculeResponseDTO> contenant les informations de tous les vehicules
     */
    @QueryHandler
    public List<VehiculeQueryDTO> on(GetAllVehiculesDTO getAllVehiculesDTO) {
        List<Vehicule> vehicules = vehiculeRepository.findAll();
        return vehicules.stream().map(VehiculeQueryMapper::convertVehiculeToVehiculeDTO).collect(Collectors.toList());
    }
}
