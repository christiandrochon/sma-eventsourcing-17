package fr.cdrochon.smamonolithe.event.command.services;

import fr.cdrochon.smamonolithe.event.commonapi.command.ClientCreateCommand;
import fr.cdrochon.smamonolithe.event.commonapi.command.GarageQueryCreateCommand;
import fr.cdrochon.smamonolithe.event.commonapi.dto.CreateClientRequestDTO;
import fr.cdrochon.smamonolithe.event.commonapi.dto.CreateGarageQueryRequestDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class GarageQueryCommandService {
    @Autowired
    private CommandGateway commandGateway;
    
    public GarageQueryCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
    
    /**
     * Genere un UUID aleatoirement pour la creation d'un id de garage
     *
     * @param createGarageQueryRequestDTO
     * @return CompletableFuture that supports dependent functions and actions triggered upon its completion
     */
    public CompletableFuture<String> createGarage(CreateGarageQueryRequestDTO createGarageQueryRequestDTO) {
        
        return commandGateway.send(new GarageQueryCreateCommand(UUID.randomUUID().toString(),
                                                                createGarageQueryRequestDTO.getNomClient(),
                                                                createGarageQueryRequestDTO.getMailResponsable()));
        
        //                                                                createGarageQueryRequestDTO.getGarageStatus(),
        //                                                                createGarageQueryRequestDTO.getDateQuery()));
    }
    
    public CompletableFuture<String> addClientToGarage(CreateClientRequestDTO createClientRequestDTO) {
        return commandGateway.send(new ClientCreateCommand(createClientRequestDTO.getNomClient(),
                                                           createClientRequestDTO.getPrenomClient()));
    }
    
    //    public CompletableFuture<String>
    
    
}
