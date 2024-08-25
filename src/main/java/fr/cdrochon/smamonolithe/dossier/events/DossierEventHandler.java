package fr.cdrochon.smamonolithe.dossier.events;

import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandDTO;
import fr.cdrochon.smamonolithe.dossier.command.services.DossierCommandService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import static fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandMapper.convertClientToClientDTO;
import static fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandMapper.convertVehiculeToVehiculeDTO;

@Component
@Slf4j
public class DossierEventHandler {
    
    private final DossierCommandService dossierCommandService;
    
    public DossierEventHandler(DossierCommandService clientCommandService) {
        this.dossierCommandService = clientCommandService;
    }
    
    
    @EventHandler
    public void on(DossierCreatedEvent event) {
        
        log.info("Received event: {}", event);
        
        Client c = event.getClient();
        //conversion du dto du dossier en entité dossier
        DossierCommandDTO dossierDTO = new DossierCommandDTO(event.getId(),
                                                             event.getNomDossier(),
                                                             event.getDateCreationDossier(),
                                                             event.getDateModificationDossier(),
                                                             convertClientToClientDTO(event.getClient()),
                                                             convertVehiculeToVehiculeDTO(event.getVehicule()),
                                                             event.getDossierStatus());
        // Compléter la future dans le service
        dossierCommandService.completeDossierCreation(dossierDTO);
    }
    
}
