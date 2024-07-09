package fr.cdrochon.smamonolithe.dossier.query.services;

import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.dossier.events.DossierCreatedEvent;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierResponseDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetDossierDTO;
import fr.cdrochon.smamonolithe.dossier.query.mapper.DossierMapper;
import fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.queryhandling.QueryHandler;
import org.hibernate.TransactionException;
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
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    private final DossierMapper dossierMapper;
    
    public DossierEventHandlerService(DossierRepository dossierRepository, ClientRepository clientRepository, VehiculeRepository vehiculeRepository,
                                      DossierMapper dossierMapper) {
        this.dossierRepository = dossierRepository;
        this.clientRepository = clientRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.dossierMapper = dossierMapper;
    }
    
    /**
     * On fait un subscribe avec @EventHandler = j'ecoute ce que fait le service DossierQueryCreatedEvent
     * <p>
     * EventMessage sert à recuperer toutes les informations sur l'event avec la methode payLoad(). Il est donc plus general que l'event créé par moi-meme
     *
     * @param event DossierCreatedEvent event qui est declenché lors de la creation d'un dossier
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
            dossier.setDossierStatus(event.getDossierStatus());
            dossier.setClient(event.getClient());
            dossier.setVehicule(event.getVehicule());
            
            Client client = new Client();
            client.setId(event.getClientId());
            client.setNomClient(event.getClient().getNomClient());
            client.setPrenomClient(event.getClient().getPrenomClient());
            client.setAdresse(event.getClient().getAdresse());
            client.setTelClient(event.getClient().getTelClient());
            client.setMailClient(event.getClient().getMailClient());
            client.setClientStatus(event.getClient().getClientStatus());
            clientRepository.save(client);
            
            Vehicule vehicule = new Vehicule();
            vehicule.setIdVehicule(event.getVehiculeId());
            vehicule.setImmatriculationVehicule(event.getVehicule().getImmatriculationVehicule());
            vehicule.setDateMiseEnCirculationVehicule(event.getVehicule().getDateMiseEnCirculationVehicule());
            vehicule.setVehiculeStatus(event.getVehicule().getVehiculeStatus());
            vehiculeRepository.save(vehicule);
            
            //            vehiculeRepository.save(event.getVehicule());
            //            clientRepository.save(event.getClient());
            dossierRepository.save(dossier);
            
        } catch(Exception e) {
            System.out.println("ERRRRRROOR : " + e.getMessage());
            throw new TransactionException("Erreur lors de la creation du dossier : " + e.getMessage());
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
        return dossiers.stream().map(DossierMapper::convertDossierToDossierDTO).collect(Collectors.toList());
    }
}
