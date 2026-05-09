package fr.cdrochon.smamonolithe.vehicule.query.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import fr.cdrochon.smamonolithe.json.Views;
import fr.cdrochon.smamonolithe.logging.BusinessLoggers;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.mapper.VehiculeQueryMapper;
import fr.cdrochon.smamonolithe.vehicule.query.repositories.VehiculeRepository;
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
public class VehiculeQueryController {
    
    private final VehiculeRepository vehiculeRepository;
    
    public VehiculeQueryController(VehiculeRepository vehiculeRepository) {
        this.vehiculeRepository = vehiculeRepository;
    }
    
    /**
     * Renvoi les informations considérées comme utiles à la partie query lors de la recherche d'un vehicule par son id.
     *
     * @param id id du vehicule
     * @return VehiculeResponseDTO
     */
    @GetMapping("/vehicules/{id}")
    @JsonView(Views.VehiculeView.class)
    //    @CircuitBreaker(name = "clientService", fallbackMethod = "getDefaultClient")
    public Mono<VehiculeQueryDTO> getDocumentByIdAsync(@PathVariable String id) {
        BusinessLoggers.business().info("BIZ_VEHICULE_READ_REQUEST vehiculeId={}", id);
        CompletableFuture<VehiculeQueryDTO> future =
                CompletableFuture.supplyAsync(() -> {
                    VehiculeQueryDTO dto = vehiculeRepository.findById(id)
                                                             .map(VehiculeQueryMapper::convertVehiculeToVehiculeDTO)
                                                             .orElse(null);
                    if(dto == null) {
                        BusinessLoggers.business().info("BIZ_VEHICULE_READ_NOT_FOUND vehiculeId={}", id);
                    } else {
                        BusinessLoggers.business().info("BIZ_VEHICULE_READ_SUCCESS vehiculeId={}", id);
                    }
                    return dto;
                });
        Mono<VehiculeQueryDTO> mono = Mono.fromFuture(future);
        return mono;
    }
    
    
    /**
     * Renvoi tous les vehicules. On n'utilise pas l'interface Repository usuelle, mais on créé une classe destinée à ca, qui renvoi le type de DTO sous forme
     * de multiples instances
     *
     * @return List<VehiculeResponseDTO> comprenant l'adresse sous forme de DTO
     */
    @GetMapping("/vehicules")
    @JsonView(Views.VehiculeView.class)
    public Flux<VehiculeQueryDTO> getDossiersAsync(Authentication authentication) {
        Jwt jwt = authentication instanceof JwtAuthenticationToken jwtAuth ? jwtAuth.getToken() : null;
        boolean isAdmin = jwt != null && hasRole(jwt, "ADMIN");
        String email = jwt != null ? jwt.getClaimAsString("email") : null;

        BusinessLoggers.business().info("BIZ_VEHICULE_LIST_REQUEST isAdmin={} email={}", isAdmin, email);
        CompletableFuture<List<VehiculeQueryDTO>> future = CompletableFuture.supplyAsync(() -> {
            List<VehiculeQueryDTO> vehicules;
            if (isAdmin || email == null) {
                vehicules = vehiculeRepository.findAll()
                        .stream()
                        .map(VehiculeQueryMapper::convertVehiculeToVehiculeDTO)
                        .collect(Collectors.toList());
            } else {
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
