package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymConvertDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import static fr.cdrochon.thymeleaffrontend.security.SecurityConfigThymeleaf.getJwtTokenValue;

@Controller
@Slf4j
public class VehiculeThymController {
    
    @Autowired
    private WebClient webClient;
    
    /**
     * Affiche les détails d'un vehicule spécifique, ou renvoi les informations saisies par l'utilisateur en cas d'erreur
     *
     * @param id                 id du vehicule
     * @param model              model de la vue vehicule/view
     * @param redirectAttributes attributs de redirection
     * @return la vue vehicule/view
     */
    @GetMapping("/vehicule/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public Mono<String> getVehiculeByIdAsync(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        return webClient.get()
                        .uri("/queries/vehicules/" + id)
                        .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                        .retrieve()
                        .bodyToMono(VehiculeThymConvertDTO.class)
                        .flatMap(dto -> {
                            assert dto != null;
                            model.addAttribute("vehicule", dto);
                            return Mono.just("vehicule/view");
                        }).onErrorResume(WebClientResponseException.class, e -> {
                    log.error("400 Bad Request: {}", e.getMessage());
                    redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                    redirectAttributes.addFlashAttribute("errorMessage", "Requête invalide.");
                    redirectAttributes.addFlashAttribute("urlRedirection", "/createVehicule");
                    return Mono.just("redirect:/error");
                });
    }
    
    
    /**
     * Affiche la liste de tous les vehicules enregistrés
     *
     * @param model              model de la vue vehicule/vehicules
     * @param redirectAttributes attributs de redirection
     * @return la vue vehicule/vehicules
     */
    @GetMapping("/vehicules")
    @PreAuthorize("hasAuthority('USER')")
    public Mono<String> getVehiculesAsync(Model model, RedirectAttributes redirectAttributes) {
        return webClient.get()
                        .uri("/queries/vehicules")
                        .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                        .retrieve()
                        .bodyToFlux(VehiculeThymConvertDTO.class)
                        .collectList()
                        .flatMap(vehicules -> {
                            assert vehicules != null;
                            model.addAttribute("vehicules", vehicules);
                            return Mono.just("vehicule/vehicules");
                        })
                        .onErrorResume(WebClientResponseException.class, e -> {
                            if(e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                                log.error("400 Bad Request: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Requête invalide.");
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createVehicule");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                                log.error("404 Not Found: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Ressource non trouvée. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createVehicule");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                                log.error("500 Internal Server Error: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage",
                                                                     "Erreur interne de serveur. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createVehicule");
                                return Mono.just("redirect:/error");
                            }
                            log.error("ERREUR: {}", e.getResponseBodyAsString());
                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                            redirectAttributes.addFlashAttribute("errorMessage", "Erreur non reconnue. " + e.getResponseBodyAsString());
                            redirectAttributes.addFlashAttribute("urlRedirection", "/vehicules");
                            return Mono.just("redirect:/error");
                        });
    }
}
