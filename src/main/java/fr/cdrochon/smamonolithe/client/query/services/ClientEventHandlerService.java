package fr.cdrochon.smamonolithe.client.query.services;

import fr.cdrochon.smamonolithe.client.events.ClientCreatedEvent;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetClientDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.mapper.ClientQueryMapper;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ClientEventHandlerService {
    
    private final ClientRepository clientRepository;
    
    
    public ClientEventHandlerService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }
    
    /**
     * Souscrit à l'événement ClientCreatedEvent sur le bus d'évènement pour sauvegarder le client dans la base de données
     *
     * @param event événement de création d'un client
     */
    @EventHandler
    public void on(ClientCreatedEvent event) {
        
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
     * @param getClientQueryDTO DTO contenant l'id du client à recuperer
     * @return ClientResponseDTO
     */
    @QueryHandler
    public ClientQueryDTO on(GetClientDTO getClientQueryDTO) {
        return clientRepository.findById(getClientQueryDTO.getId()).map(ClientQueryMapper::convertClientToClientDTO)
                               .orElseThrow(() -> new EntityNotFoundException("Client non trouvé"));
    }
    
    /**
     * Recupere tous les clients
     *
     * @return List<ClientResponseDTO>
     */
    @QueryHandler
    public List<ClientQueryDTO> on() {
        List<Client> clients = clientRepository.findAll();
        return clients.stream().map(ClientQueryMapper::convertClientToClientDTO).collect(Collectors.toList());
    }
}
