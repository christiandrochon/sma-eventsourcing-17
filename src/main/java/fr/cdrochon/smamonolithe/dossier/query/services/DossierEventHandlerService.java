package fr.cdrochon.smamonolithe.dossier.query.services;

import fr.cdrochon.smamonolithe.client.events.ClientCreatedEvent;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientResponseDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetClientDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.mapper.ClientMapper;
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
    public void on(ClientCreatedEvent event, EventMessage<ClientCreatedEvent> eventMessage) {
        log.info("********************************");
        log.info("ClientQueryCreatedEvent received !!!!!!!!!!!!!!!!!!!!!!");
        log.info("Identifiant d'evenement : " + event.getId());
        log.info("Identifiant d'agregat : " + eventMessage.getIdentifier());
        
        try {
            Client client = new Client();
            client.setId(event.getId());
            client.setNomClient(event.getNomClient());
            client.setPrenomClient(event.getPrenomClient());
            client.setMailClient(event.getMailClient());
            client.setTelClient(event.getTelClient());
            client.setAdresse(event.getAdresseClient());
            client.setClientStatus(event.getClientStatus());
            
            clientRepository.save(client);
        } catch(Exception e) {
            System.out.println("ERRRRRRRRRRRRRRRRRRRROOR : " + e.getMessage());
        }
    }
    
    /**
     * Recupere un client avec son id
     *
     * @param getClientQueryDTO
     * @return ClientResponseDTO
     */
    @QueryHandler
    public ClientResponseDTO on(GetClientDTO getClientQueryDTO) {
        //        ClientResponseDTO clientResponseDTO = clientRepository.findById(getClientQueryDTO.getId()).map(ClientMapper::convertClientToClientDTO).get();
        return clientRepository.findById(getClientQueryDTO.getId()).map(ClientMapper::convertClientToClientDTO)
                               .orElseThrow(() -> new EntityNotFoundException("Client non trouvé"));
    }
    
    /**
     * Recupere tous les clients
     *
     * @return List<ClientResponseDTO>
     */
    @QueryHandler
    public List<ClientResponseDTO> on() {
        List<Client> clients = clientRepository.findAll();
        List<ClientResponseDTO> clientResponseDTOS = clients.stream().map(ClientMapper::convertClientToClientDTO).collect(Collectors.toList());
        return clients.stream().map(ClientMapper::convertClientToClientDTO).collect(Collectors.toList());
    }
}
