package fr.cdrochon.smamonolithe.client.events;

import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import fr.cdrochon.smamonolithe.client.command.services.ClientCommandService;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import static fr.cdrochon.smamonolithe.client.query.mapper.AdresseMapper.convertAdresseToClientAdresseDTO;

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
        ClientAdresseDTO adresseDTO = convertAdresseToClientAdresseDTO(event.getAdresseClient());
        ClientCommandDTO clientDTO = new ClientCommandDTO(event.getId(), event.getNomClient(), event.getPrenomClient(), event.getMailClient(),
                                                          event.getTelClient(), adresseDTO, event.getClientStatus());
        
        // Compléter la future dans le service
        clientCommandService.completeClientCreation(clientDTO);
    }
}
