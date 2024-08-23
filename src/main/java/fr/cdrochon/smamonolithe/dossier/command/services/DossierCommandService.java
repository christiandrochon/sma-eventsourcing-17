package fr.cdrochon.smamonolithe.dossier.command.services;

import fr.cdrochon.smamonolithe.dossier.command.commands.DossierCreateCommand;
import fr.cdrochon.smamonolithe.dossier.command.dtos.DossierRestDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class DossierCommandService {
    private final CommandGateway commandGateway;
    public DossierCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
    
    /**
     * Genere un UUID aleatoirement pour la creation d'un id de dossier, de client et de vehicule
     *
     * @param dossierRestDTO DTO contenant les informations du dossier a creer
     * @return CompletableFuture that supports dependent functions and actions triggered upon its completion
     */
    @Transactional
    public CompletableFuture<String> createDossier(DossierRestDTO dossierRestDTO) {
        System.out.println("ClientCommandService.createClient");
        
        dossierRestDTO.getClient().setId(UUID.randomUUID().toString());
        dossierRestDTO.getVehicule().setIdVehicule(UUID.randomUUID().toString());
        
        return commandGateway.send(new DossierCreateCommand(UUID.randomUUID().toString(),
                                                            dossierRestDTO.getNomDossier(),
                                                            dossierRestDTO.getDateCreationDossier(),
                                                            dossierRestDTO.getDateModificationDossier(),
                                                            dossierRestDTO.getClient(),
                                                            dossierRestDTO.getVehicule(),
                                                            dossierRestDTO.getDossierStatus(),
                                                            dossierRestDTO.getClient().getId(),
                                                            dossierRestDTO.getVehicule().getIdVehicule()
        ));
    }
}
