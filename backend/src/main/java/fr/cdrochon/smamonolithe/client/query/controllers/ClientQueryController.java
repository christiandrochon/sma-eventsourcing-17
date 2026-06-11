package fr.cdrochon.smamonolithe.client.query.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientListResponse;
import fr.cdrochon.smamonolithe.client.query.dtos.GetAllClientsDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.GetClientDTO;
import fr.cdrochon.smamonolithe.client.query.mapper.ClientQueryMapper;
import fr.cdrochon.smamonolithe.json.Views;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Clients - Queries", description = "Requêtes de lecture CQRS liées aux clients")
@RestController
@RequestMapping(path = "/queries")
@Slf4j
public class ClientQueryController {

    private final QueryGateway queryGateway;

    public ClientQueryController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    /**
     * Méthode asynchrone qui renvoi un client dto.
     * ADMIN peut lire tous les clients
     * USER ne peut lire que ses propres clients (ceux liés à son userId)
     *
     * @param id id du client
     * @return Mono de ClientResponseDTO
     */
    @Operation(
            summary = "Récupérer un client par ID",
            description = "Récupère les informations d'un client spécifique. Les utilisateurs USER ne peuvent accéder qu'à leurs propres données."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Client trouvé",
                    content = @Content(schema = @Schema(implementation = ClientQueryDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Accès refusé - client non autorisé"),
            @ApiResponse(responseCode = "404", description = "Client non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
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
      @Operation(
              summary = "Lister tous les clients",
              description = "Récupère la liste de tous les clients. Les utilisateurs USER ne voient que leurs propres données, les ADMIN et AUDITOR voient tous les clients."
      )
      @ApiResponses({
              @ApiResponse(
                      responseCode = "200",
                      description = "Liste des clients",
                      content = @Content(schema = @Schema(implementation = ClientQueryDTO.class))
              ),
              @ApiResponse(responseCode = "403", description = "Accès refusé"),
              @ApiResponse(responseCode = "500", description = "Erreur serveur")
      })
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
    @Operation(
            summary = "Regarder les mises à jour d'un client en temps réel",
            description = "Retourne un flux d'événements en temps réel pour les mises à jour d'un client via Server-Sent Events (SSE)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Flux de mises à jour du client",
                    content = @Content(schema = @Schema(implementation = ClientQueryDTO.class))
            ),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
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

    /**
     * Définit si un role est contenu dans le jwt
     * @param jwt token
     * @param role role de l'user
     * @return role contenu ou pas dans le jwt
     */
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
            BusinessLoggers.business().info("BIZ_CLIENT_READ_STREAM_FAILED role={}", role);
        }
        return false;
    }
}
