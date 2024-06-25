package fr.cdrochon.smamonolithe.event.command.services;

import fr.cdrochon.smamonolithe.event.commonapi.command.GarageQueryCreateCommand;
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
    
    public CompletableFuture<String> createGarage(CreateGarageQueryRequestDTO createGarageQueryRequestDTO) {
        RandomStringGenerator randomStringGenerator = new RandomStringGenerator();
        String s = randomStringGenerator.generateRandomString(10);
        //UUID.randomUUID().toString()
        return commandGateway.send(new GarageQueryCreateCommand(UUID.randomUUID().toString(),
                                                                createGarageQueryRequestDTO.getNomClient(),
                                                                createGarageQueryRequestDTO.getMailResponsable()));
                                                                
//                                                                createGarageQueryRequestDTO.getGarageStatus(),
//                                                                createGarageQueryRequestDTO.getDateQuery()));
    }
}
