package fr.cdrochon.smamonolithe.client.events;

import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import fr.cdrochon.smamonolithe.client.command.services.ClientCommandService;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.garage.query.dto.GarageAdresseDTO;
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
        
//        ClientAdresseDTO adresseDTO = new ClientAdresseDTO(
//                event.getAdresseClient().getNumeroDeRue(),
//                event.getAdresseClient().getRue(),
//                event.getAdresseClient().getComplementAdresse(),
//                event.getAdresseClient().getCp(),
//                event.getAdresseClient().getVille(),
//                event.getAdresseClient().getPays()
//        );
        ClientCommandDTO clientDTO = new ClientCommandDTO(event.getId(), event.getNomClient(), event.getPrenomClient(), event.getMailClient(),
                                                          event.getTelClient(), adresseDTO, event.getClientStatus());
        
        
        log.info("ClientCommandDTO : {}", clientDTO);
        
        // Compléter la future dans le service
        clientCommandService.completeClientCreation(clientDTO);
    }
}
