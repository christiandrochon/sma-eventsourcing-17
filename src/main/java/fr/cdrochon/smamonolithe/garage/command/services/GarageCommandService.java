package fr.cdrochon.smamonolithe.garage.command.services;

import fr.cdrochon.smamonolithe.garage.commonapi.command.ClientCreateCommand;
import fr.cdrochon.smamonolithe.garage.commonapi.command.GarageCreateCommand;
import fr.cdrochon.smamonolithe.garage.commonapi.dto.ClientRequestDTO;
import fr.cdrochon.smamonolithe.garage.commonapi.dto.GarageRequestDTO;
import fr.cdrochon.smamonolithe.garage.commonapi.dto.GarageRestPostDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class GarageCommandService {
    
    private final CommandGateway commandGateway;
    
    public GarageCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
    
    /**
     * Genere un UUID aleatoirement pour la creation d'un id de garage
     *
     * @param createGarageQueryRequestDTO
     * @return CompletableFuture that supports dependent functions and actions triggered upon its completion
     */
    public CompletableFuture<String> createGarage(GarageRestPostDTO createGarageQueryRequestDTO) {
        
        return commandGateway.send(new GarageCreateCommand(UUID.randomUUID().toString(),
                                                           createGarageQueryRequestDTO.getNomGarage(),
                                                           createGarageQueryRequestDTO.getEmailContactGarage(),
                                                           createGarageQueryRequestDTO.getAdresseGarage()
                                   ));
        //                                                                createGarageQueryRequestDTO.getDateQuery()));
    }
    
    public CompletableFuture<String> addClientToGarage(ClientRequestDTO createClientRequestDTO) {
        return commandGateway.send(new ClientCreateCommand(createClientRequestDTO.getId(), createClientRequestDTO.getNomClient(),
                                                           createClientRequestDTO.getPrenomClient()));
    }
    
    //    public CompletableFuture<String>
    
    
}
