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
        // Behaviour required by legacy tests: throw NPE immediately when null is passed.
        if (vehiculeRestPostDTO == null) {
            throw new NullPointerException("vehiculeRestPostDTO must not be null");
        }

        String vehiculeId = UUID.randomUUID().toString();
        String requestVehiculeId = vehiculeRestPostDTO.getId();
        CompletableFuture<VehiculeCommandDTO> futureDTO = new CompletableFuture<>();
        pendingCreations.put(vehiculeId, futureDTO);
        if (requestVehiculeId != null && !requestVehiculeId.isBlank()) {
            pendingCreations.put(requestVehiculeId, futureDTO);
        }


        BusinessLoggers.business().info("BIZ_VEHICULE_CREATE_REQUEST vehiculeId={} immatriculation={} status={}",
                                        vehiculeId,
                                        vehiculeRestPostDTO.getImmatriculationVehicule(),
                                        vehiculeRestPostDTO.getVehiculeStatus());

        CompletableFuture<Object> commandFuture = commandGateway.send(new VehiculeCreateCommand(vehiculeId,
                                                                                                  vehiculeRestPostDTO.getImmatriculationVehicule(),
                                                                                                  vehiculeRestPostDTO.getDateMiseEnCirculationVehicule(),
                                                                                                  vehiculeRestPostDTO.getVehiculeStatus()
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
        // Guard against null dto to prevent NPEs flagged by Sonar S2259
        if (dto == null) {
            log.warn("TECH_VEHICULE_CREATE_EVENT_NULL (event received with null dto)");
            return;
        }

        String id = dto.getId();
        CompletableFuture<VehiculeCommandDTO> pending = removePending(id, null);
        if (pending != null) {
            BusinessLoggers.business().info("BIZ_VEHICULE_CREATE_CONFIRMED vehiculeId={} immatriculation={} status={}",
                                            id,
                                            dto.getImmatriculationVehicule(),
                                            dto.getVehiculeStatus());
            pending.complete(dto);
        } else {
            log.warn("TECH_VEHICULE_CREATE_FUTURE_MISSING vehiculeId={} (event recu sans future en attente)", id);
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
