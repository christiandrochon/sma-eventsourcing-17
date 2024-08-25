package fr.cdrochon.smamonolithe.vehicule.command.services;

import fr.cdrochon.smamonolithe.document.command.dtos.DocumentCommandDTO;
import fr.cdrochon.smamonolithe.vehicule.command.commands.VehiculeCreateCommand;
import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class VehiculeCommandService {
    
    private final CommandGateway commandGateway;
    //utiliser une CompletableFuture pour synchroniser l'attente du contrôleur jusqu'à ce que l'événement soit reçu et traité
    private CompletableFuture<VehiculeCommandDTO> futureDTO;
    
    public VehiculeCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
    
    /**
     * Genere un UUID aleatoirement pour la creation d'un id de vehicule
     *
     * @param vehiculeRestPostDTO DTO contenant les informations du vehicule a creer
     * @return CompletableFuture that supports dependent functions and actions triggered upon its completion
     */
    @Transactional
    public CompletableFuture<VehiculeCommandDTO> createVehicule(VehiculeCommandDTO vehiculeRestPostDTO) {
        //CompletableFuture<DossierCommandDTO> sera complétée lorsque l'événement sera reçu.
        futureDTO = new CompletableFuture<>();
        commandGateway.send(new VehiculeCreateCommand(UUID.randomUUID().toString(),
                                                      vehiculeRestPostDTO.getImmatriculationVehicule(),
                                                      vehiculeRestPostDTO.getDateMiseEnCirculationVehicule(),
                                                      vehiculeRestPostDTO.getVehiculeStatus()
        ));
        return futureDTO;
    }
    
    /**
     * Compléter la future dans le service. Méthode appelée par @EventHandler
     *
     * @param dto DTO de création d'un garage
     */
    public void completeVehiculeCreation(VehiculeCommandDTO dto) {
        if(futureDTO != null) {
            futureDTO.complete(dto);
        }
    }
}
