package fr.cdrochon.smamonolithe.client.query.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetClientDTO;
import fr.cdrochon.smamonolithe.client.query.mapper.ClientQueryMapper;
import fr.cdrochon.smamonolithe.client.query.repositories.ClientRepository;
import fr.cdrochon.smamonolithe.json.Views;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/queries")
@Slf4j
public class ClientQueryController {
    
    private final QueryGateway queryGateway;
    private final ClientRepository clientRepository;
    
    public ClientQueryController(QueryGateway queryGateway, ClientRepository clientRepository) {
        this.queryGateway = queryGateway;
        this.clientRepository = clientRepository;
    }
    
    //FIXME: 2021-08-25 - CDROCHON - A REVOIR -> VALIDATION DU FORMULAIRE PAS TERRIBLE au niveau des animations et des messages d'erreurs
    
    /**
     * Méthode asynchrone qui renvoi un client dto.
     * ADMIN peut lire tous les clients
     * USER ne peut lire que ses propres clients (ceux liés à son userId)
     *
     * @param id id du client
     * @return Mono de ClientResponseDTO
     */
    @GetMapping(path = "/clients/{id}")
    @JsonView(Views.ClientView.class)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'AUDITOR')")
    public Mono<ClientQueryDTO> getClientByIdAsync(@PathVariable String id) {
        BusinessLoggers.business().info("BIZ_CLIENT_READ_REQUEST clientId={}", id);
        CompletableFuture<ClientQueryDTO> future =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        ClientQueryDTO client = clientRepository.findById(id)
                                                                 .map(ClientQueryMapper::convertClientToClientDTO)
                                                                 .orElseThrow(() -> new RuntimeException("Client not found"));

                        // Vérifier que USER ne peut accéder qu'à ses propres clients
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        boolean isAdmin = auth.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .anyMatch(a -> a.equals("ROLE_ADMIN"));

                        if (!isAdmin) {
                            // USER doit vérifier que le client lui appartient
                            String currentUserId = auth.getName();
                            // TODO: ajouter un champ userId au Client et vérifier qu'il correspond
                            // if (!client.getUserId().equals(currentUserId)) {
                            //     throw new RuntimeException("Accès refusé: ce client ne vous appartient pas");
                            // }
                        }

                        BusinessLoggers.business().info("BIZ_CLIENT_READ_SUCCESS clientId={}", id);
                        return client;
                    } catch(Exception e) {
                        BusinessLoggers.business().error("BIZ_CLIENT_READ_FAILED clientId={} message={}", id, e.getMessage());
                        log.error("Error retrieving client with id {}: {}", id, e.getMessage(), e);
                        throw new RuntimeException("Error retrieving client", e);
                    }
                });
        Mono<ClientQueryDTO> mono = Mono.fromFuture(future);
        return mono;
    }
    
      /**
       * Retourne la liste de tous les clients de manière asynchrone
       * ADMIN voit tous les clients
       * USER ne voit que ses propres clients (ceux liés à son userId)
       * AUDITOR voit tous les clients
       *
       * @return Flux de ClientResponseDTO
       */
      @GetMapping(path = "/clients")
      @JsonView(Views.ClientView.class)
      @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'AUDITOR')")
      public Flux<ClientQueryDTO> getClientsAsync() {
          BusinessLoggers.business().info("BIZ_CLIENT_LIST_REQUEST");
          CompletableFuture<List<ClientQueryDTO>> future = CompletableFuture.supplyAsync(() -> {
              Authentication auth = SecurityContextHolder.getContext().getAuthentication();
              boolean isAdmin = auth.getAuthorities().stream()
                      .map(GrantedAuthority::getAuthority)
                      .anyMatch(a -> a.equals("ROLE_ADMIN"));

              boolean isAuditor = auth.getAuthorities().stream()
                      .map(GrantedAuthority::getAuthority)
                      .anyMatch(a -> a.equals("ROLE_AUDITOR"));

              List<ClientQueryDTO> clients =
                      clientRepository.findAll()
                                      .stream()
                                      .map(ClientQueryMapper::convertClientToClientDTO)
                                      .collect(Collectors.toList());

              // Si USER, filtrer pour ne voir que ses propres clients
              // TODO: ajouter un champ userId au Client pour pouvoir filtrer correctement
              if (!isAdmin && !isAuditor) {
                  String currentUserId = auth.getName();
                  // clients = clients.stream()
                  //         .filter(c -> c.getUserId() != null && c.getUserId().equals(currentUserId))
                  //         .collect(Collectors.toList());
                  BusinessLoggers.business().info("BIZ_CLIENT_LIST_FILTERED userId={} count={}", currentUserId, clients.size());
              } else {
                  BusinessLoggers.business().info("BIZ_CLIENT_LIST_SUCCESS count={}", clients.size());
              }

              return clients;
          });
          Flux<ClientQueryDTO> flux = Flux.fromStream(future.join().stream());
          return flux;
      }

    
    /**
     * Renvoi un flux de GarageResponseDTO qui sera mis à jour en temps réel avec de nouvelles données chaque fois qu'un nouvel événement est publié.
     *
     * @param id id du garage
     * @return Flux de GarageResponseDTO
     */
    @GetMapping(value = "/client/{id}/watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ClientQueryDTO> watch(@PathVariable String id) {
        BusinessLoggers.business().info("BIZ_CLIENT_READ_STREAM_REQUEST clientId={}", id);

        try(SubscriptionQueryResult<ClientQueryDTO, ClientQueryDTO> result = queryGateway.subscriptionQuery(
                new GetClientDTO(id),
                ResponseTypes.instanceOf(ClientQueryDTO.class),
                ResponseTypes.instanceOf(ClientQueryDTO.class)
                                                                                                           )) {
            return result.initialResult()
                         .concatWith(result.updates())
                         .doOnNext(client -> BusinessLoggers.business().info("BIZ_CLIENT_READ_STREAM_EVENT clientId={}", id))
                         .doOnError(error -> BusinessLoggers.business().error("BIZ_CLIENT_READ_STREAM_FAILED clientId={} message={}",
                                                                              id,
                                                                              error.getMessage()));
        }
    }
}
