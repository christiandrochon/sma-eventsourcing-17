package fr.cdrochon.smamonolithe.dossier.query.services;

import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.dossier.events.DossierCreatedEvent;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierResponseDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetDossierDTO;
import fr.cdrochon.smamonolithe.dossier.query.mapper.DossierMapper;
import fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository;
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
public class DossierEventHandlerService {
    
    private final DossierRepository dossierRepository;
    private final DossierMapper dossierMapper;
    
    public DossierEventHandlerService(DossierRepository dossierRepository, DossierMapper dossierMapper) {
        this.dossierRepository = dossierRepository;
        this.dossierMapper = dossierMapper;
    }
    
    /**
     * On fait un subscribe avec @EventHandler = j'ecoute ce que fait le service DossierQueryCreatedEvent
     * <p>
     * EventMessage sert à recuperer toutes les informations sur l'event avec la methode payLoad(). Il est donc plus general que l'event créé par moi-meme
     *
     * @param event l'event GarageQueryCreatedEvent
     */
    @EventHandler
    public void on(DossierCreatedEvent event, EventMessage<DossierCreatedEvent> eventMessage) {
        log.info("********************************");
        log.info("DossierCreatedEvent received !!!!!!!!!!!!!!!!!!!!!!");
        log.info("Identifiant d'evenement : " + event.getId());
        log.info("Identifiant d'agregat : " + eventMessage.getIdentifier());
        
        try {
            Dossier dossier = new Dossier();
            dossier.setId(event.getId());
            dossier.setNomDossier(event.getNomDossier());
            dossier.setDateCreationDossier(event.getDateCreationDossier());
            dossier.setDateModificationDossier(event.getDateModificationDossier());
            dossier.setClient(event.getClient());
            dossier.setVehicule(event.getVehicule());
            dossier.setDossierStatus(event.getDossierStatus());
            
            
        } catch(Exception e) {
            System.out.println("ERRRRRROOR : " + e.getMessage());
        }
    }
    
    /**
     * Recupere un dossier avec son id
     *
     * @param getDossierDTO DTO contenant l'id du dossier a recuperer
     * @return DossierResponseDTO contenant les informations du dossier
     */
    @QueryHandler
    public DossierResponseDTO on(GetDossierDTO getDossierDTO) {
        //        ClientResponseDTO clientResponseDTO = clientRepository.findById(getClientQueryDTO.getId()).map(ClientMapper::convertClientToClientDTO).get();
        return dossierRepository.findById(getDossierDTO.getId()).map(DossierMapper::convertDossierToDossierDTO)
                                .orElseThrow(() -> new EntityNotFoundException("Dossier non trouvé"));
    }
    
    /**
     * Recupere tous les dossiers
     *
     * @return List<DossierResponseDTO> contenant les informations de tous les dossiers
     */
    @QueryHandler
    public List<DossierResponseDTO> on() {
        List<Dossier> dossiers = dossierRepository.findAll();
        List<DossierResponseDTO> dossierResponseDTOS = dossiers.stream().map(DossierMapper::convertDossierToDossierDTO).collect(Collectors.toList());
        return dossiers.stream().map(DossierMapper::convertDossierToDossierDTO).collect(Collectors.toList());
    }
}
