package fr.cdrochon.smamonolithe.vehicule.event;

import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
import fr.cdrochon.smamonolithe.vehicule.command.services.VehiculeCommandService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VehiculeEventHandler {
    
    private final VehiculeCommandService vehiculeCommandService;
    
    public VehiculeEventHandler(VehiculeCommandService vehiculeCommandService) {
        this.vehiculeCommandService = vehiculeCommandService;
    }
    
    /**
     * Complete le future dans le service VehiculeCommandService lorsqu'un VehiculeCreatedEvent est reçu
     *
     * @param event VehiculeCreatedEvent event qui est declenché lors de la creation d'un vehicule
     */
    @EventHandler
    public void on(VehiculeCreatedEvent event) {
        log.info("Received event: {}", event);
        VehiculeCommandDTO commandDTO = new VehiculeCommandDTO(event.getId(),
                                                               event.getImmatriculationVehicule(),
                                                               event.getDateMiseEnCirculationVehicule(),
                                                               event.getVehiculeStatus());
        // Compléter la future dans le service
        vehiculeCommandService.completeVehiculeCreation(commandDTO);
    }
}
