package fr.cdrochon.smamonolithe.client.command.services;

import fr.cdrochon.smamonolithe.client.command.commands.ClientCreateCommand;
import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ClientCommandService {
    
    private final CommandGateway commandGateway;
    //utiliser une CompletableFuture pour synchroniser l'attente du contrôleur jusqu'à ce que l'événement soit reçu et traité
    private CompletableFuture<ClientCommandDTO> futureDTO;
    
    public ClientCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
    
    /**
     * Genere un UUID aleatoirement pour la creation d'un id de client
     *
     * @param clientrestPostDTO DTO contenant les informations du client a creer
     * @return CompletableFuture that supports dependent functions and actions triggered upon its completion
     */
    public CompletableFuture<ClientCommandDTO> createClient(ClientCommandDTO clientrestPostDTO) {
        //CompletableFuture<GarageCommandDTO> sera complétée lorsque l'événement sera reçu.
        futureDTO = new CompletableFuture<>();
        //envoyer la commande de création de garage -> @CommandHandler
        //CHECKME : est ce ici que l'on créé l'id du garage ?
        commandGateway.send(new ClientCreateCommand(UUID.randomUUID().toString(),
                                                    clientrestPostDTO.getNomClient(),
                                                    clientrestPostDTO.getPrenomClient(),
                                                    clientrestPostDTO.getMailClient(),
                                                    clientrestPostDTO.getTelClient(),
                                                    clientrestPostDTO.getAdresse()));
        return futureDTO;
    }
    
    /**
     * Compléter la future dans le service. Méthode appelée par @EventHandler
     *
     * @param dto DTO de création d'un garage
     */
    public void completeClientCreation(ClientCommandDTO dto) {
        if(futureDTO != null) {
            futureDTO.complete(dto);
        }
    }
}
