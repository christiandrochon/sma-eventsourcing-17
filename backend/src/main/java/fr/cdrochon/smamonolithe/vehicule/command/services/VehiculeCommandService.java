package fr.cdrochon.smamonolithe.vehicule.command.services;

import fr.cdrochon.smamonolithe.vehicule.command.commands.VehiculeCreateCommand;
import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.events.VehiculeCreatedApplicationEvent;
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
public class VehiculeCommandService {

    private static final long CREATE_TIMEOUT_SECONDS = 20L;

    private final CommandGateway commandGateway;
    private final ConcurrentMap<String, CompletableFuture<VehiculeCommandDTO>> pendingCreations = new ConcurrentHashMap<>();

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
        String vehiculeId = UUID.randomUUID().toString();
        String requestVehiculeId = vehiculeRestPostDTO != null ? vehiculeRestPostDTO.getId() : null;
        CompletableFuture<VehiculeCommandDTO> futureDTO = new CompletableFuture<>();
        pendingCreations.put(vehiculeId, futureDTO);
        if(requestVehiculeId != null && !requestVehiculeId.isBlank()) {
            pendingCreations.put(requestVehiculeId, futureDTO);
        }

        // Avoid dereferencing a possibly-null DTO (fix Sonar S2259)
        String immatriculationVehicule = vehiculeRestPostDTO != null ? vehiculeRestPostDTO.getImmatriculationVehicule() : null;
        String dateMiseEnCirculationVehicule = vehiculeRestPostDTO != null ? vehiculeRestPostDTO.getDateMiseEnCirculationVehicule() : null;
        String vehiculeStatus = vehiculeRestPostDTO != null ? vehiculeRestPostDTO.getVehiculeStatus() : null;

        if (vehiculeRestPostDTO == null) {
            BusinessLoggers.business().error("BIZ_VEHICULE_CREATE_REQUEST_INVALID vehiculeId={} payload=null", vehiculeId);
            // Cleanup pending entries and complete exceptionally
            removePending(vehiculeId, requestVehiculeId);
            futureDTO.completeExceptionally(new IllegalArgumentException("vehiculeRestPostDTO must not be null"));
            return futureDTO.orTimeout(CREATE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                            .whenComplete((ok, err) -> removePending(vehiculeId, requestVehiculeId));
        }

        BusinessLoggers.business().info("BIZ_VEHICULE_CREATE_REQUEST vehiculeId={} immatriculation={} status= {}",
                                        vehiculeId,
                                        immatriculationVehicule,
                                        vehiculeStatus);

        CompletableFuture<Object> commandFuture = commandGateway.send(new VehiculeCreateCommand(vehiculeId,
                                                                                                  immatriculationVehicule,
                                                                                                  dateMiseEnCirculationVehicule,
                                                                                                  vehiculeStatus
        ));

        if(commandFuture != null) {
            commandFuture.whenComplete((ignored, error) -> {
                if(error != null) {
                    CompletableFuture<VehiculeCommandDTO> pending = removePending(vehiculeId, requestVehiculeId);
                    if(pending != null) {
                        BusinessLoggers.business().error("BIZ_VEHICULE_CREATE_FAILED vehiculeId={} message={}",
                                                         vehiculeId,
                                                         error.getMessage());
                        pending.completeExceptionally(error);
                    }
                }
            });
        }

        return futureDTO.orTimeout(CREATE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .whenComplete((ok, err) -> removePending(vehiculeId, requestVehiculeId));
    }

    /**
     * Compléter la future dans le service. Méthode appelée par @EventHandler
     *
     * @param dto DTO de création d'un garage
     */
    public void completeVehiculeCreation(VehiculeCommandDTO dto) {
        CompletableFuture<VehiculeCommandDTO> pending = removePending(dto != null ? dto.getId() : null, null);
        if(pending != null) {
            BusinessLoggers.business().info("BIZ_VEHICULE_CREATE_CONFIRMED vehiculeId={} immatriculation={} status={}",
                                            dto.getId(),
                                            dto.getImmatriculationVehicule(),
                                            dto.getVehiculeStatus());
            pending.complete(dto);
        } else {
            log.warn("TECH_VEHICULE_CREATE_FUTURE_MISSING vehiculeId={} (event recu sans future en attente)", dto.getId());
        }
    }

    /**
     * Listener Spring qui reçoit l'événement VehiculeCreatedApplicationEvent publié par VehiculeEventHandlerService
     * après que le vehicule ait été persiste en DB. Complète la CompletableFuture en attente.
     *
     * @param event VehiculeCreatedApplicationEvent contenant le DTO du vehicule créé
     */
    @EventListener
    public void onVehiculeCreatedApplicationEvent(VehiculeCreatedApplicationEvent event) {
        if(event != null && event.getVehicule() != null) {
            VehiculeCommandDTO vehiculeDTO = new VehiculeCommandDTO();
            VehiculeQueryDTO queryDTO = event.getVehicule();
            vehiculeDTO.setId(queryDTO.getId());
            vehiculeDTO.setImmatriculationVehicule(queryDTO.getImmatriculationVehicule());
            vehiculeDTO.setDateMiseEnCirculationVehicule(queryDTO.getDateMiseEnCirculationVehicule());
            vehiculeDTO.setVehiculeStatus(queryDTO.getVehiculeStatus());

            completeVehiculeCreation(vehiculeDTO);
        }
    }

    private CompletableFuture<VehiculeCommandDTO> removePending(String primaryId, String secondaryId) {
        CompletableFuture<VehiculeCommandDTO> pending = null;
        if(primaryId != null && !primaryId.isBlank()) {
            pending = pendingCreations.remove(primaryId);
        }
        if(secondaryId != null && !secondaryId.isBlank() && !secondaryId.equals(primaryId)) {
            CompletableFuture<VehiculeCommandDTO> secondaryPending = pendingCreations.remove(secondaryId);
            if(pending == null) {
                pending = secondaryPending;
            }
        }
        return pending;
    }
}
