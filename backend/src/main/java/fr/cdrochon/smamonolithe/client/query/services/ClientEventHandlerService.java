package fr.cdrochon.smamonolithe.client.query.services;

import fr.cdrochon.smamonolithe.client.events.ClientCreatedEvent;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientListResponse;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetAllClientsDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetClientDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.client.query.events.ClientCreatedApplicationEvent;
import fr.cdrochon.smamonolithe.client.query.mapper.ClientQueryMapper;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ClientEventHandlerService {

    private final ClientRepository clientRepository;
    private final ApplicationEventPublisher applicationEventPublisher;


    public ClientEventHandlerService(ClientRepository clientRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.clientRepository = clientRepository;
        this.applicationEventPublisher = applicationEventPublisher;
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
            BusinessLoggers.business().info("BIZ_CLIENT_CREATED clientId={} status={}", client.getId(),
                client.getClientStatus());

            // Publier un événement Spring pour notifier les listeners (notamment ClientCommandService)
            ClientQueryDTO clientDTO = ClientQueryMapper.convertClientToClientDTO(client);
            applicationEventPublisher.publishEvent(new ClientCreatedApplicationEvent(this, clientDTO));
        } catch (Exception e) {
            log.error("TECH_CLIENT_PERSIST_ERROR clientId={} message={}", event.getId(), e.getMessage(), e);
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
     * @param query requete Axon de recuperation de tous les clients
     * @return ClientListResponse contenant la liste des clients
     */
    @QueryHandler
    public ClientListResponse on(GetAllClientsDTO query) {
        List<Client> clients = clientRepository.findAll();
        List<ClientQueryDTO> dtos = clients.stream().map(ClientQueryMapper::convertClientToClientDTO).collect(Collectors.toList());
        return new ClientListResponse(dtos);
    }

    // Compatibilite tests/unit legacy: conserve la signature historique sans argument.
    public List<ClientQueryDTO> on() {
        return on(new GetAllClientsDTO()).getItems();
    }
}
