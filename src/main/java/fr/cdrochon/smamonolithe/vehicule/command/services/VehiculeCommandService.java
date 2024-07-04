package fr.cdrochon.smamonolithe.vehicule.command.services;

import fr.cdrochon.smamonolithe.vehicule.command.commands.VehiculeCreateCommand;
import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeRestPostDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class VehiculeCommandService {
    
    private final CommandGateway commandGateway;
    
    public VehiculeCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
    
    /**
     * Genere un UUID aleatoirement pour la creation d'un id de vehicule
     *
     * @param vehiculeRestPostDTO DTO contenant les informations du vehicule a creer
     * @return CompletableFuture that supports dependent functions and actions triggered upon its completion
     */
    public CompletableFuture<String> createVehicule(VehiculeRestPostDTO vehiculeRestPostDTO) {
        System.out.println("ClientCommandService.createClient");
        return commandGateway.send(new VehiculeCreateCommand(UUID.randomUUID().toString(),
                                                             vehiculeRestPostDTO.getImmatriculationVehicule(),
                                                             vehiculeRestPostDTO.getDateMiseEnCirculationVehicule()
                                                             
        ));
       
    }
}
