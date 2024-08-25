package fr.cdrochon.smamonolithe.dossier.command.services;

import fr.cdrochon.smamonolithe.dossier.command.commands.DossierCreateCommand;
import fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandDTO;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandMapper.convertClientDtoToClient;
import static fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandMapper.convertVehiculeDtoToVehicule;


@Service
@Slf4j
public class DossierCommandService {
    
    private final CommandGateway commandGateway;
    //utiliser une CompletableFuture pour synchroniser l'attente du contrôleur jusqu'à ce que l'événement soit reçu et traité
    private CompletableFuture<DossierCommandDTO> futureDTO;
    
    public DossierCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
    
    /**
     * Genere un UUID aleatoirement pour la creation d'un id de dossier, lequel doit servir à l'agregat. La generatio ndes ids de client et vehicule se feront
     * au niveau du QueryService, avec le @EventHandler
     *
     * @param dossierCommandDTO DTO contenant les informations du dossier a creer
     * @return CompletableFuture that supports dependent functions and actions triggered upon its completion
     */
    @Transactional
    public CompletableFuture<DossierCommandDTO> createDossier(DossierCommandDTO dossierCommandDTO) {
        //CompletableFuture<DossierCommandDTO> sera complétée lorsque l'événement sera reçu.
        futureDTO = new CompletableFuture<>();
        //envoi de la commande de création du dossier
        commandGateway.send(new DossierCreateCommand(UUID.randomUUID().toString(),
                                                     dossierCommandDTO.getNomDossier(),
                                                     dossierCommandDTO.getDateCreationDossier(),
                                                     dossierCommandDTO.getDateModificationDossier(),
                                                     convertClientDtoToClient(dossierCommandDTO.getClient()),
                                                     convertVehiculeDtoToVehicule(dossierCommandDTO.getVehicule()),
                                                     dossierCommandDTO.getDossierStatus(),
                                                     dossierCommandDTO.getClient().getId(),
                                                     dossierCommandDTO.getVehicule().getId()));
        return futureDTO;
    }
    
    /**
     * Compléter la future dans le service. Méthode appelée par @EventHandler
     *
     * @param dto DTO de création d'un garage
     */
    public void completeDossierCreation(DossierCommandDTO dto) {
        if(futureDTO != null) {
            futureDTO.complete(dto);
        }
    }
}
