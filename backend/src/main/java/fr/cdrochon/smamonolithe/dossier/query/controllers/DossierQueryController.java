package fr.cdrochon.smamonolithe.dossier.query.controllers;

import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierListResponse;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetAllDossiersDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.GetDossierDTO;
import fr.cdrochon.smamonolithe.dossier.query.mapper.DossierQueryMapper;
import fr.cdrochon.smamonolithe.dossier.query.repositories.DossierRepository;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import lombok.val;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
public class DossierQueryController {
    
    private final QueryGateway queryGateway;
    private final DossierRepository dossierRepository;
    
    public DossierQueryController(QueryGateway queryGateway, DossierRepository dossierRepository) {
        this.queryGateway = queryGateway;
        this.dossierRepository = dossierRepository;
    }
    
    /**
     * Renvoi les informations utiles à la partie query lors d'une recherche. Il y a moins d'informations que dans l'objet renvoyé pour l'affichage d'un client
     * à un concessionnaire
     *
     * @param id id du dossier
     * @return DossierResponseDTO avec les informations utiles pour la partie query
     */
    @GetMapping(path = "/dossiers/{id}")
    public Mono<DossierQueryDTO> getDossierByIdAsync(@PathVariable String id) {
        BusinessLoggers.business().info("BIZ_DOSSIER_READ_REQUEST dossierId={}", id);
        CompletableFuture<DossierQueryDTO> future =
                CompletableFuture.supplyAsync(() -> {
                    val dossier = queryGateway.query(new GetDossierDTO(id), ResponseTypes.instanceOf(DossierQueryDTO.class)).join();
                    if(dossier == null) {
                        BusinessLoggers.business().info("BIZ_DOSSIER_READ_NOT_FOUND dossierId={}", id);
                    } else {
                        BusinessLoggers.business().info("BIZ_DOSSIER_READ_SUCCESS dossierId={}", id);
                    }
                    return dossier;
                });
        Mono<DossierQueryDTO> mono = Mono.fromFuture(future);
        return mono;
    }
    
    /**
     * Pur trouver tous les dossiers, on n'utilise pas l'interface Repository usuelle, mais on créé une classe destinée à ca, qui renvoi le type de DTO sous
     * forme de multiples instances
     *
     * @return List<DossierResponseDTO> liste des dossiers sous forme de DTO
     */
    @GetMapping(path = "/dossiers")
    public Flux<DossierQueryDTO> getDossiersAsync(Authentication authentication) {
        Jwt jwt = extractJwt(authentication);
        boolean isAdmin = jwt != null && hasRole(jwt, "ADMIN");
        String email = jwt != null ? jwt.getClaimAsString("email") : null;

        BusinessLoggers.business().info("BIZ_DOSSIER_LIST_REQUEST isAdmin={} email={}", isAdmin, email);

        CompletableFuture<List<DossierQueryDTO>> future = CompletableFuture.supplyAsync(() -> {
            DossierListResponse response = queryGateway.query(new GetAllDossiersDTO(), ResponseTypes.instanceOf(DossierListResponse.class)).join();
            List<DossierQueryDTO> dossiers = response.getItems();
            if (!isAdmin && email != null) {
                dossiers = dossiers.stream().filter(dossier -> dossier.getClient() != null && email.equals(dossier.getClient().getMailClient())).collect(Collectors.toList());
            }
            BusinessLoggers.business().info("BIZ_DOSSIER_LIST_SUCCESS count={}", dossiers.size());
            return dossiers;
        });
        return Flux.fromStream(future.join().stream());
    }

    private static Jwt extractJwt(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        return null;
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
        } catch (Exception ignored) {}
        return false;
    }
    
    /**
     * Attend que le dossier soit disponible dans la projection (read model).
     * Utile après une création pour attendre la synchronisation de l'event sourcing.
     *
     * @param id id du dossier
     * @return Mono<DossierQueryDTO> le dossier quand il est disponible
     */
    @GetMapping(path = "/dossiers/{id}/wait-ready")
    public Mono<DossierQueryDTO> waitForDossierReady(@PathVariable String id) {
        BusinessLoggers.business().info("BIZ_DOSSIER_WAIT_READY_REQUEST dossierId={}", id);

        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            long maxWaitMs = 5000; // Attendre max 5 secondes
            long pollingIntervalMs = 100; // Vérifier toutes les 100ms
            long startTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTime < maxWaitMs) {
                DossierQueryDTO dossier = dossierRepository.findById(id)
                        .map(DossierQueryMapper::convertDossierToDossierDTO)
                        .orElse(null);

                if (dossier != null) {
                    BusinessLoggers.business().info("BIZ_DOSSIER_WAIT_READY_OK dossierId={} waitMs={}",
                            id, System.currentTimeMillis() - startTime);
                    return dossier;
                }

                try {
                    Thread.sleep(pollingIntervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    BusinessLoggers.business().warn("BIZ_DOSSIER_WAIT_READY_INTERRUPTED dossierId={}", id);
                    return null;
                }
            }

            BusinessLoggers.business().warn("BIZ_DOSSIER_WAIT_READY_TIMEOUT dossierId={}", id);
            return null;
        }))
        .onErrorResume(e -> {
            BusinessLoggers.business().error("BIZ_DOSSIER_WAIT_READY_ERROR dossierId={} message={}", id, e.getMessage());
            return Mono.empty();
        });
    }

    /**
     * Renvoi un flux de DossierResponseDTO qui sera mis à jour en temps réel avec de nouvelles données chaque fois qu'un nouvel événement est publié.
     *
     * @param id id du dossier
     * @return Flux de DossierResponseDTO
     */
    @GetMapping(value = "/dossier/{id}/watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DossierQueryDTO> watch(@PathVariable String id) {
        BusinessLoggers.business().info("BIZ_DOSSIER_READ_STREAM_REQUEST dossierId={}", id);

        try(SubscriptionQueryResult<DossierQueryDTO, DossierQueryDTO> result = queryGateway.subscriptionQuery(
                new GetDossierDTO(id),
                ResponseTypes.instanceOf(DossierQueryDTO.class),
                ResponseTypes.instanceOf(DossierQueryDTO.class))) {
            return result.initialResult()
                         .concatWith(result.updates())
                         .doOnNext(dossier -> BusinessLoggers.business().info("BIZ_DOSSIER_READ_STREAM_EVENT dossierId={}", id))
                         .doOnError(error -> BusinessLoggers.business().error("BIZ_DOSSIER_READ_STREAM_FAILED dossierId={} message={}",
                                                                              id,
                                                                              error.getMessage()));
        }
    }
}
