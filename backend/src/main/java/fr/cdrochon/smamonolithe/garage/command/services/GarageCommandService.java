package fr.cdrochon.smamonolithe.garage.command.services;

import fr.cdrochon.smamonolithe.garage.command.commands.GarageCreateCommand;
import fr.cdrochon.smamonolithe.garage.command.dtos.GarageCommandDTO;
import fr.cdrochon.smamonolithe.garage.query.dto.GarageQueryDTO;
import fr.cdrochon.smamonolithe.garage.query.events.GarageCreatedApplicationEvent;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GarageCommandService {
    
    private static final long CREATE_TIMEOUT_SECONDS = 20L;

    private final CommandGateway commandGateway;
    private final ConcurrentMap<String, CompletableFuture<GarageCommandDTO>> pendingCreations = new ConcurrentHashMap<>();
    
    public GarageCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
    
    /**
     * @param garageDTO DTO de création d'un garage
     * @return CompletableFuture<GarageCommandDTO> sera complétée lorsque l'événement sera reçu
     */
    @Transactional
    public CompletableFuture<GarageCommandDTO> createGarage(GarageCommandDTO garageDTO) {
        String garageId = UUID.randomUUID().toString();
        CompletableFuture<GarageCommandDTO> futureGarageDTO = new CompletableFuture<>();
        pendingCreations.put(garageId, futureGarageDTO);

        BusinessLoggers.business().info("BIZ_GARAGE_CREATE_REQUEST garageId={} nomGarage={}",
                                        garageId,
                                        garageDTO.getNomGarage());

        commandGateway.send(new GarageCreateCommand(garageId, garageDTO.getNomGarage(), garageDTO.getMailResp(), garageDTO.getAdresse()))
                      .whenComplete((ignored, error) -> {
                          if(error != null) {
                              CompletableFuture<GarageCommandDTO> pending = pendingCreations.remove(garageId);
                              if(pending != null) {
                                  BusinessLoggers.business().error("BIZ_GARAGE_CREATE_FAILED garageId={} message={}",
                                                                  garageId,
                                                                  error.getMessage());
                                  pending.completeExceptionally(error);
                              }
                          }
                      });

        return futureGarageDTO.orTimeout(CREATE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                              .whenComplete((ok, err) -> pendingCreations.remove(garageId));
    }
    
    /**
     * Compléter la future dans le service. Méthode appelée par @EventHandler
     *
     * @param garageDTO DTO de création d'un garage
     */
    public void completeGarageCreation(GarageCommandDTO garageDTO) {
        CompletableFuture<GarageCommandDTO> pending = pendingCreations.remove(garageDTO.getId());
        if(pending != null) {
            BusinessLoggers.business().info("BIZ_GARAGE_CREATE_CONFIRMED garageId={} nomGarage={}",
                                            garageDTO.getId(),
                                            garageDTO.getNomGarage());
            pending.complete(garageDTO);
        }
    }
    
    /**
     * Listener Spring qui reçoit l'événement GarageCreatedApplicationEvent publié par GarageEventHandlerService
     * après que le garage ait été persiste en DB. Complète la CompletableFuture en attente.
     *
     * @param event GarageCreatedApplicationEvent contenant le DTO du garage créé
     */
    @EventListener
    public void onGarageCreatedApplicationEvent(GarageCreatedApplicationEvent event) {
        if(event != null && event.getGarage() != null) {
            GarageCommandDTO garageDTO = new GarageCommandDTO();
            GarageQueryDTO queryDTO = event.getGarage();
            garageDTO.setId(queryDTO.getId());
            garageDTO.setNomGarage(queryDTO.getNomGarage());
            garageDTO.setMailResp(queryDTO.getMailResp());
            garageDTO.setAdresse(queryDTO.getAdresse());
            
            completeGarageCreation(garageDTO);
        }
    }
    
}
