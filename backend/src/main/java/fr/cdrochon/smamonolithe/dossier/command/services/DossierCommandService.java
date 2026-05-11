package fr.cdrochon.smamonolithe.dossier.command.services;

import fr.cdrochon.smamonolithe.dossier.command.commands.DossierCreateCommand;
import fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.events.DossierCreatedApplicationEvent;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandMapper.convertClientDtoToClient;
import static fr.cdrochon.smamonolithe.dossier.command.dtos.DossierCommandMapper.convertVehiculeDtoToVehicule;


@Service
@Slf4j
public class DossierCommandService {
    
    private static final long CREATE_TIMEOUT_SECONDS = 20L;

    private final CommandGateway commandGateway;
    private final java.util.Map<String, CompletableFuture<DossierCommandDTO>> dossierFutures = new java.util.concurrent.ConcurrentHashMap<>();

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
        String dossierId = UUID.randomUUID().toString();
        BusinessLoggers.business().info("BIZ_DOSSIER_CREATE_REQUEST dossierId={} nomDossier={} clientId={} vehiculeId={} status={}",
                                        dossierId,
                                        dossierCommandDTO.getNomDossier(),
                                        dossierCommandDTO.getClient() != null ? dossierCommandDTO.getClient().getId() : null,
                                        dossierCommandDTO.getVehicule() != null ? dossierCommandDTO.getVehicule().getId() : null,
                                        dossierCommandDTO.getDossierStatus());

        CompletableFuture<DossierCommandDTO> dossierFuture = new CompletableFuture<>();
        dossierFutures.put(dossierId, dossierFuture);

        DossierCreateCommand command = new DossierCreateCommand(
                dossierId,
                dossierCommandDTO.getNomDossier(),
                dossierCommandDTO.getDateCreationDossier(),
                dossierCommandDTO.getDateModificationDossier(),
                convertClientDtoToClient(dossierCommandDTO.getClient()),
                convertVehiculeDtoToVehicule(dossierCommandDTO.getVehicule()),
                dossierCommandDTO.getDossierStatus(),
                dossierCommandDTO.getClient() != null ? dossierCommandDTO.getClient().getId() : null,
                dossierCommandDTO.getVehicule() != null ? dossierCommandDTO.getVehicule().getId() : null,
                dossierCommandDTO.getUserId()
        );

        commandGateway.send(command).whenComplete((ignored, error) -> {
            if(error != null) {
                CompletableFuture<DossierCommandDTO> pending = dossierFutures.remove(dossierId);
                if(pending != null) {
                    BusinessLoggers.business().error("BIZ_DOSSIER_CREATE_FAILED dossierId={} message={}",
                                                     dossierId,
                                                     error.getMessage());
                    pending.completeExceptionally(error);
                }
            }
        });

        return dossierFuture.orTimeout(CREATE_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                           .whenComplete((ok, err) -> dossierFutures.remove(dossierId));
    }
    
    /**
     * Compléter la future dans le service. Méthode appelée par @EventHandler
     *
     * @param dto DTO de création d'un dossier
     */
    public void completeDossierCreation(DossierCommandDTO dto) {
        BusinessLoggers.business().info("BIZ_DOSSIER_CREATE_CONFIRMED dossierId={} clientId={} vehiculeId={} status={}",
                dto != null ? dto.getId() : null,
                dto != null && dto.getClient() != null ? dto.getClient().getId() : null,
                dto != null && dto.getVehicule() != null ? dto.getVehicule().getId() : null,
                dto != null ? dto.getDossierStatus() : null);
        
        if(dto != null && dto.getId() != null) {
            CompletableFuture<DossierCommandDTO> future = dossierFutures.remove(dto.getId());
            if(future != null) {
                future.complete(dto);
            } else {
                log.warn("TECH_DOSSIER_CREATE_FUTURE_MISSING dossierId={} (event recu sans future en attente)", dto.getId());
            }
        }
    }

    /**
     * Listener Spring qui reçoit l'événement DossierCreatedApplicationEvent publié par DossierEventHandlerService
     * après que le dossier ait été persiste en DB. Complète la CompletableFuture en attente.
     *
     * @param event DossierCreatedApplicationEvent contenant le DTO du dossier créé
     */
    @EventListener
    public void onDossierCreatedApplicationEvent(DossierCreatedApplicationEvent event) {
        if(event != null && event.getDossier() != null) {
            DossierCommandDTO dossierDTO = new DossierCommandDTO();
            DossierQueryDTO queryDTO = event.getDossier();
            dossierDTO.setId(queryDTO.getId());
            dossierDTO.setNomDossier(queryDTO.getNomDossier());
            dossierDTO.setDateCreationDossier(queryDTO.getDateCreationDossier());
            dossierDTO.setDateModificationDossier(queryDTO.getDateModificationDossier());
            dossierDTO.setDossierStatus(queryDTO.getDossierStatus());
            
            completeDossierCreation(dossierDTO);
        }
    }
}
