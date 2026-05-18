package fr.cdrochon.smamonolithe.client.query.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientListResponse;
import fr.cdrochon.smamonolithe.client.query.dtos.GetAllClientsDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetClientDTO;
import fr.cdrochon.smamonolithe.client.query.mapper.ClientQueryMapper;
import fr.cdrochon.smamonolithe.json.Views;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
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
    
    public ClientQueryController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
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
    public Mono<ClientQueryDTO> getClientByIdAsync(@PathVariable String id, Authentication authentication) {
        BusinessLoggers.business().info("BIZ_CLIENT_READ_REQUEST clientId={}", id);

        Jwt jwt = authentication instanceof JwtAuthenticationToken jwtAuth ? jwtAuth.getToken() : null;
        boolean isPrivileged = jwt != null && (hasRole(jwt, "ADMIN") || hasRole(jwt, "AUDITOR"));
        String email = jwt != null ? jwt.getClaimAsString("email") : null;

        CompletableFuture<ClientQueryDTO> future = CompletableFuture.supplyAsync(() -> {
            try {
                ClientQueryDTO client = queryGateway.query(new GetClientDTO(id), ResponseTypes.instanceOf(ClientQueryDTO.class)).join();
                if (client == null) {
                    BusinessLoggers.business().info("BIZ_CLIENT_READ_NOT_FOUND clientId={}", id);
                    return null;
                }

                if (!isPrivileged) {
                    if (email == null) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Acces refuse: utilisateur sans email JWT");
                    }
                    String ownerEmail = client.getMailClient();
                    if (!email.equals(ownerEmail)) {
                        BusinessLoggers.business().warn("BIZ_CLIENT_READ_FORBIDDEN clientId={} email={}", id, email);
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Accès refusé : ce client ne vous appartient pas");
                    }
                }

                BusinessLoggers.business().info("BIZ_CLIENT_READ_SUCCESS clientId={}", id);
                return client;
            } catch(ResponseStatusException e) {
                throw e;
            } catch(Exception e) {
                BusinessLoggers.business().error("BIZ_CLIENT_READ_FAILED clientId={} message={}", id, e.getMessage());
                log.error("Error retrieving client with id {}: {}", id, e.getMessage(), e);
                throw new RuntimeException("Error retrieving client", e);
            }
        });
        return Mono.fromFuture(future);
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
      public Flux<ClientQueryDTO> getClientsAsync(Authentication authentication) {
          BusinessLoggers.business().info("BIZ_CLIENT_LIST_REQUEST");
          Jwt jwt = authentication instanceof JwtAuthenticationToken jwtAuth ? jwtAuth.getToken() : null;
          boolean isPrivileged = jwt != null && (hasRole(jwt, "ADMIN") || hasRole(jwt, "AUDITOR"));
          String email = jwt != null ? jwt.getClaimAsString("email") : null;

          CompletableFuture<List<ClientQueryDTO>> future = CompletableFuture.supplyAsync(() -> {
              ClientListResponse response = queryGateway.query(new GetAllClientsDTO(), ResponseTypes.instanceOf(ClientListResponse.class)).join();
              List<ClientQueryDTO> clients = response.getItems();

              if (!isPrivileged) {
                  if (email == null) {
                      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                              "Acces refuse: utilisateur sans email JWT");
                  }
                  clients = clients.stream()
                          .filter(c -> email.equals(c.getMailClient()))
                          .collect(Collectors.toList());
                  BusinessLoggers.business().info("BIZ_CLIENT_LIST_FILTERED email={} count={}", email, clients.size());
              } else {
                  BusinessLoggers.business().info("BIZ_CLIENT_LIST_SUCCESS count={}", clients.size());
              }

              return clients;
          });
          return Mono.fromFuture(future).flatMapMany(Flux::fromIterable);
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

    private boolean hasRole(Jwt jwt, String role) {
        try {
            java.util.Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null) {
                Object roles = realmAccess.get("roles");
                if (roles instanceof java.util.List<?> list) {
                    return list.contains(role) || list.contains("ROLE_" + role);
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
