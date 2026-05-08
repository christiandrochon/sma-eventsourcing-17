package fr.cdrochon.smamonolithe.vehicule.command.services;

import fr.cdrochon.smamonolithe.vehicule.command.commands.VehiculeCreateCommand;
import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
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
        String vehiculeId = UUID.randomUUID().toString();
        log.info("BIZ_VEHICULE_CREATE_REQUEST vehiculeId={} immatriculation={} status={}",
                 vehiculeId,
                 vehiculeRestPostDTO.getImmatriculationVehicule(),
                 vehiculeRestPostDTO.getVehiculeStatus());
        commandGateway.send(new VehiculeCreateCommand(vehiculeId,
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
            log.info("BIZ_VEHICULE_CREATE_CONFIRMED vehiculeId={} immatriculation={} status={}",
                     dto.getId(),
                     dto.getImmatriculationVehicule(),
                     dto.getVehiculeStatus());
            futureDTO.complete(dto);
        }
    }
}
