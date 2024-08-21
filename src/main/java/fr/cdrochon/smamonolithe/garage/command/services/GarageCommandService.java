package fr.cdrochon.smamonolithe.garage.command.services;

import fr.cdrochon.smamonolithe.garage.command.commands.GarageCreateCommand;
import fr.cdrochon.smamonolithe.garage.command.dtos.GarageCommandDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class GarageCommandService {
    
    private final CommandGateway commandGateway;
    
    //utiliser une CompletableFuture pour synchroniser l'attente du contrôleur jusqu'à ce que l'événement soit reçu et traité
    private CompletableFuture<GarageCommandDTO> futureGarageDTO;
    
    public GarageCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
    
    /**
     *
     * @param garageDTO DTO de création d'un garage
     * @return CompletableFuture<GarageCommandDTO> sera complétée lorsque l'événement sera reçu
     */
    public CompletableFuture<GarageCommandDTO> createGarage(GarageCommandDTO garageDTO) {
        //CompletableFuture<GarageCommandDTO> sera complétée lorsque l'événement sera reçu.
        futureGarageDTO = new CompletableFuture<>();
        //envoyer la commande de création de garage -> @CommandHandler
        //CHECKME : est ce ici que l'on créé l'id du garage ?
        commandGateway.send(new GarageCreateCommand(UUID.randomUUID().toString(), garageDTO.getNomGarage(), garageDTO.getMailResp(), garageDTO.getAdresse()));
        return futureGarageDTO;
    }
    
    /**
     * Compléter la future dans le service. Méthode appelée par @EventHandler
     *
     * @param garageDTO DTO de création d'un garage
     */
    public void completeGarageCreation(GarageCommandDTO garageDTO) {
        if(futureGarageDTO != null) {
            futureGarageDTO.complete(garageDTO);
        }
    }
    
}
