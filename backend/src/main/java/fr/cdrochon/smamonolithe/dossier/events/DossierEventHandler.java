package fr.cdrochon.smamonolithe.dossier.events;

import fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandDTO;
import fr.cdrochon.smamonolithe.dossier.command.services.DossierCommandService;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
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
         BusinessLoggers.business().info("BIZ_DOSSIER_CREATED dossierId={} nomDossier={} clientId={} vehiculeId={} status={}",
                                        event.getId(), event.getNomDossier(), event.getClientId(), event.getVehiculeId(), event.getDossierStatus());
         //conversion du dto du dossier en entité dossier
         DossierCommandDTO dossierDTO = DossierCommandDTO.builder()
                 .id(event.getId())
                 .nomDossier(event.getNomDossier())
                 .dateCreationDossier(event.getDateCreationDossier())
                 .dateModificationDossier(event.getDateModificationDossier())
                 .client(convertClientToClientDTO(event.getClient()))
                 .vehicule(convertVehiculeToVehiculeDTO(event.getVehicule()))
                 .dossierStatus(event.getDossierStatus())
                 .userId(event.getUserId())
                 .build();
         // Compléter la future dans le service
         dossierCommandService.completeDossierCreation(dossierDTO);
     }

}
