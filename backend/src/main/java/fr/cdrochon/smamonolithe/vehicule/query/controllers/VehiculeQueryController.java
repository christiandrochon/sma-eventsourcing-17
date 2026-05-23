package fr.cdrochon.smamonolithe.vehicule.query.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import fr.cdrochon.smamonolithe.json.Views;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.query.mapper.VehiculeQueryMapper;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Tag(name = "Vehicules - Queries", description = "Requêtes de lecture CQRS liées aux véhicules")
@RestController
@RequestMapping(path = "/queries")
public class VehiculeQueryController {
    
    private final VehiculeRepository vehiculeRepository;
    
    public VehiculeQueryController(VehiculeRepository vehiculeRepository) {
        this.vehiculeRepository = vehiculeRepository;
    }
    
    /**
     * Retourne les informations d'un véhicule par son id.
     * RBAC / IDOR fix : ADMIN voit tout, USER ne peut voir que ses propres véhicules.
     */
    @Operation(
            summary = "Récupérer un véhicule par ID",
            description = "Récupère les informations d'un véhicule spécifique. Les utilisateurs USER ne peuvent accéder qu'à leurs propres véhicules."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Véhicule trouvé",
                    content = @Content(schema = @Schema(implementation = VehiculeQueryDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Accès refusé - véhicule non autorisé"),
            @ApiResponse(responseCode = "404", description = "Véhicule non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @GetMapping("/vehicules/{id}")
    @JsonView(Views.VehiculeView.class)
    public Mono<VehiculeQueryDTO> getVehiculeByIdAsync(@PathVariable String id,
                                                       Authentication authentication) {
        Jwt jwt = authentication instanceof JwtAuthenticationToken jwtAuth ? jwtAuth.getToken() : null;
        boolean isAdmin = jwt != null && hasRole(jwt, "ADMIN");
        String email    = jwt != null ? jwt.getClaimAsString("email") : null;

        BusinessLoggers.business().info("BIZ_VEHICULE_READ_REQUEST vehiculeId={} isAdmin={}", id, isAdmin);

        return Mono.fromCallable(() -> {
            Vehicule vehicule = vehiculeRepository.findById(id).orElse(null);
            if (vehicule == null) {
                BusinessLoggers.business().info("BIZ_VEHICULE_READ_NOT_FOUND vehiculeId={}", id);
                return null;
            }
            // IDOR fix : un USER ne peut consulter que ses propres véhicules
            if (!isAdmin) {
                if (email == null) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Acces refuse: utilisateur sans email JWT");
                }
                String ownerEmail = vehicule.getClient() != null
                        ? vehicule.getClient().getMailClient()
                        : null;
                if (!email.equals(ownerEmail)) {
                    BusinessLoggers.business().warn(
                            "BIZ_VEHICULE_READ_FORBIDDEN vehiculeId={} email={}", id, email);
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Accès refusé : ce véhicule ne vous appartient pas");
                }
            }
            BusinessLoggers.business().info("BIZ_VEHICULE_READ_SUCCESS vehiculeId={}", id);
            return VehiculeQueryMapper.convertVehiculeToVehiculeDTO(vehicule);
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    
    /**
     * Renvoi tous les vehicules. On n'utilise pas l'interface Repository usuelle, mais on créé une classe destinée à ca, qui renvoi le type de DTO sous forme
     * de multiples instances
     *
     * @return List<VehiculeResponseDTO> comprenant l'adresse sous forme de DTO
     */
    @Operation(
            summary = "Lister tous les véhicules",
            description = "Récupère la liste de tous les véhicules. Les utilisateurs USER ne voient que leurs propres véhicules, les ADMIN voient tous les véhicules."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des véhicules",
                    content = @Content(schema = @Schema(implementation = VehiculeQueryDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @GetMapping("/vehicules")
    @JsonView(Views.VehiculeView.class)
    public Flux<VehiculeQueryDTO> getDossiersAsync(Authentication authentication) {
        Jwt jwt = authentication instanceof JwtAuthenticationToken jwtAuth ? jwtAuth.getToken() : null;
        boolean isAdmin = jwt != null && hasRole(jwt, "ADMIN");
        String email = jwt != null ? jwt.getClaimAsString("email") : null;

        BusinessLoggers.business().info("BIZ_VEHICULE_LIST_REQUEST isAdmin={} email={}", isAdmin, email);
        CompletableFuture<List<VehiculeQueryDTO>> future = CompletableFuture.supplyAsync(() -> {
            List<VehiculeQueryDTO> vehicules;
            if (isAdmin) {
                vehicules = vehiculeRepository.findAll()
                        .stream()
                        .map(VehiculeQueryMapper::convertVehiculeToVehiculeDTO)
                        .collect(Collectors.toList());
            } else {
                if (email == null) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Acces refuse: utilisateur sans email JWT");
                }
                vehicules = vehiculeRepository.findByClientMailClient(email)
                        .stream()
                        .map(VehiculeQueryMapper::convertVehiculeToVehiculeDTO)
                        .collect(Collectors.toList());
            }
            BusinessLoggers.business().info("BIZ_VEHICULE_LIST_SUCCESS count={}", vehicules.size());
            return vehicules;
        });
        return Flux.fromStream(future.join().stream());
    }

    // Compatibilite tests unitaires existants (sans couche web/security).
    public Mono<VehiculeQueryDTO> getDocumentByIdAsync(String id) {
        return Mono.fromCallable(() -> vehiculeRepository.findById(id)
                .map(VehiculeQueryMapper::convertVehiculeToVehiculeDTO)
                .orElse(null));
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
}
