package fr.cdrochon.smamonolithe.garage.events;

import fr.cdrochon.smamonolithe.garage.command.dtos.GarageCommandDTO;
import fr.cdrochon.smamonolithe.garage.command.services.GarageCommandService;
import fr.cdrochon.smamonolithe.garage.query.dto.GarageAdresseDTO;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GarageEventHandler {
    
    private final GarageCommandService garageCommandService;
    
    public GarageEventHandler(GarageCommandService garageCommandService) {
        this.garageCommandService = garageCommandService;
    }
    
    /**
     * Souscrit à l'événement GarageCreatedEvent sur le bus d'évènement pour compléter la création du garage.
     *
     * @param event événement de création d'un garage
     */
    @EventHandler
    public void on(GarageCreatedEvent event) {
        // Convertir l'événement en DTO si nécessaire
        log.info("Received event: {}", event);
        
        GarageAdresseDTO adresseDTO = new GarageAdresseDTO(
                event.getAdresseGarage().getNumeroDeRue(),
                event.getAdresseGarage().getRue(),
                event.getAdresseGarage().getCp(),
                event.getAdresseGarage().getVille()
        );
        GarageCommandDTO garageDTO = new GarageCommandDTO(event.getId(), event.getNomGarage(), event.getMailResponsable(), adresseDTO);
        
        // Compléter la future dans le service
        garageCommandService.completeGarageCreation(garageDTO);
    }
    
}
