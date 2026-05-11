package fr.cdrochon.smamonolithe.client.command.services;

import fr.cdrochon.smamonolithe.client.command.commands.ClientCreateCommand;
import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.events.ClientCreatedApplicationEvent;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ClientCommandService {
    
    private static final long CREATE_TIMEOUT_SECONDS = 20L;

    private final CommandGateway commandGateway;
    private final ConcurrentMap<String, CompletableFuture<ClientCommandDTO>> pendingCreations = new ConcurrentHashMap<>();
    
    public ClientCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
    
    /**
     * Genere un UUID aleatoirement pour la creation d'un id de client
     *
     * @param clientrestPostDTO DTO contenant les informations du client a creer
     * @return CompletableFuture that supports dependent functions and actions triggered upon its completion
     */
    @Transactional
    public CompletableFuture<ClientCommandDTO> createClient(ClientCommandDTO clientrestPostDTO) {
        String clientId = UUID.randomUUID().toString();
        CompletableFuture<ClientCommandDTO> futureDTO = new CompletableFuture<>();
        pendingCreations.put(clientId, futureDTO);

        BusinessLoggers.business().info("BIZ_CLIENT_CREATE_REQUEST clientId={} nomClient={}", clientId,
                                        clientrestPostDTO.getNomClient());

        commandGateway.send(new ClientCreateCommand(clientId,
                                                    clientrestPostDTO.getNomClient(),
                                                    clientrestPostDTO.getPrenomClient(),
                                                    clientrestPostDTO.getMailClient(),
                                                    clientrestPostDTO.getTelClient(),
                                                    clientrestPostDTO.getAdresse()))
                      .whenComplete((ignored, error) -> {
                          if(error != null) {
                              CompletableFuture<ClientCommandDTO> pending = pendingCreations.remove(clientId);
                              if(pending != null) {
                                  BusinessLoggers.business().error("BIZ_CLIENT_CREATE_FAILED clientId={} message={}",
                                                                  clientId,
                                                                  error.getMessage());
                                  pending.completeExceptionally(error);
                              }
                          }
                      });

        return futureDTO.orTimeout(CREATE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .whenComplete((ok, err) -> pendingCreations.remove(clientId));
    }
    
    /**
     * Compléter la future dans le service. Méthode appelée par @EventHandler
     *
     * @param dto DTO de création d'un garage
     */
    public void completeClientCreation(ClientCommandDTO dto) {
        CompletableFuture<ClientCommandDTO> pending = pendingCreations.remove(dto.getId());
        if(pending != null) {
            BusinessLoggers.business().info("BIZ_CLIENT_CREATE_CONFIRMED clientId={} status={}", dto.getId(),
                                            dto.getClientStatus());
            pending.complete(dto);
        } else {
            log.warn("TECH_CLIENT_CREATE_FUTURE_MISSING clientId={} (event recu sans future en attente)", dto.getId());
        }
    }
    
    /**
     * Listener Spring qui reçoit l'événement ClientCreatedApplicationEvent publié par ClientEventHandlerService
     * après que le client ait été persiste en DB. Complète la CompletableFuture en attente.
     *
     * @param event ClientCreatedApplicationEvent contenant le DTO du client créé
     */
    @EventListener
    public void onClientCreatedApplicationEvent(ClientCreatedApplicationEvent event) {
        if(event != null && event.getClient() != null) {
            ClientCommandDTO clientDTO = new ClientCommandDTO();
            ClientQueryDTO queryDTO = event.getClient();
            clientDTO.setId(queryDTO.getId());
            clientDTO.setNomClient(queryDTO.getNomClient());
            clientDTO.setPrenomClient(queryDTO.getPrenomClient());
            clientDTO.setMailClient(queryDTO.getMailClient());
            clientDTO.setTelClient(queryDTO.getTelClient());
            clientDTO.setAdresse(queryDTO.getAdresse());
            clientDTO.setClientStatus(queryDTO.getClientStatus());
            
            completeClientCreation(clientDTO);
        }
    }
}
