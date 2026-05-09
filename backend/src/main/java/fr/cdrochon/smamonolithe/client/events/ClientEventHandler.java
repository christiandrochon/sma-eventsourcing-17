package fr.cdrochon.smamonolithe.client.events;

import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import fr.cdrochon.smamonolithe.client.command.services.ClientCommandService;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import static fr.cdrochon.smamonolithe.client.query.mapper.AdresseQueryMapper.convertAdresseToClientAdresseDTO;

@Component
@Slf4j
public class ClientEventHandler {
    
    private final ClientCommandService clientCommandService;
    
    public ClientEventHandler(ClientCommandService clientCommandService) {
        this.clientCommandService = clientCommandService;
    }
    
    @EventHandler
    public void on(ClientCreatedEvent event) {
        
        log.info("Received event: {}", event);
        BusinessLoggers.business().info("BIZ_CLIENT_CREATED clientId={} nomClient={} prenomClient={} status={}",
                                       event.getId(), event.getNomClient(), event.getPrenomClient(), event.getClientStatus());
        ClientAdresseDTO adresseDTO = convertAdresseToClientAdresseDTO(event.getAdresseClient());
        ClientCommandDTO clientDTO = new ClientCommandDTO(event.getId(), event.getNomClient(), event.getPrenomClient(), event.getMailClient(),
                                                          event.getTelClient(), adresseDTO, event.getClientStatus());
        
        // Compléter la future dans le service
        clientCommandService.completeClientCreation(clientDTO);
    }
}
