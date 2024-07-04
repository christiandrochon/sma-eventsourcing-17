package fr.cdrochon.smamonolithe.client.command.services;

import fr.cdrochon.smamonolithe.client.command.commands.ClientCreateCommand;
import fr.cdrochon.smamonolithe.client.command.dtos.ClientRestPostDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ClientCommandService {
    
    private final CommandGateway commandGateway;
    
    public ClientCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
    
    /**
     * Genere un UUID aleatoirement pour la creation d'un id de client
     *
     * @param clientrestPostDTO
     * @return CompletableFuture that supports dependent functions and actions triggered upon its completion
     */
    public CompletableFuture<String> createClient(ClientRestPostDTO clientrestPostDTO) {
        System.out.println("ClientCommandService.createClient");
        return commandGateway.send(new ClientCreateCommand(UUID.randomUUID().toString(),
                                                           clientrestPostDTO.getNomClient(),
                                                           clientrestPostDTO.getPrenomClient(),
                                                           clientrestPostDTO.getMailClient(),
                                                           clientrestPostDTO.getTelClient(),
                                                           clientrestPostDTO.getAdresse()
        ));
    }
}
