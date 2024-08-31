package fr.cdrochon.thymeleaffrontend.controller.dossier;

import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierThymConvertDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierThymDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
@Slf4j
public class DossierThymController {
    
    @Autowired
    private WebClient webClient;
    
    /**
     * Affiche les détails d'un dossier spécifique, ou renvoi les informations saisies par l'utilisateur en cas d'erreur
     *
     * @param id    id du dossier
     * @param model model de la vue dossier/view
     * @return la vue dossier/view
     */
    @GetMapping(value = "/dossier/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public Mono<String> getClientByIdAsync(@PathVariable String id, Model model) {
        return webClient.get()
                        .uri("/queries/dossiers/" + id)
                        //                                  .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " +
                        //                                  getJwtTokenValue()))
                        .retrieve()
                        .bodyToMono(DossierThymConvertDTO.class)
                        .onErrorResume(throwable -> Mono.error(new RuntimeException("Erreur lors de la récupération du dossier")))
                        .flatMap(dto -> {
                            assert dto != null;
                            model.addAttribute("dossier", dto);
                            return Mono.just("dossier/view");
                        });
    }
    
    /**
     * Affiche la liste de tous les dossiers enregistrés
     *
     * @param model              model de la vue dossier/dossiers
     * @param redirectAttributes attributs de redirection
     * @return la vue dossier/dossiers
     */
    @GetMapping(value = "/dossiers")
    @PreAuthorize("hasAuthority('USER')")
    public Mono<String> getDossiersAsync(Model model, RedirectAttributes redirectAttributes) {
        return webClient.get()
                        .uri("/queries/dossiers")
                        //                                         .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION,
                        //                                         "Bearer " + getJwtTokenValue()))
                        .retrieve()
                        .bodyToFlux(DossierThymDTO.class)
                        .collectList()
                        .flatMap(dossiers -> {
                            assert dossiers != null;
                            model.addAttribute("dossiers", dossiers);
                            return Mono.just("dossier/dossiers");
                        })
                        .onErrorResume(WebClientResponseException.class, e -> {
                            if(e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                                log.error("400 Bad Request: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Requête invalide.");
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createGarage");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                                log.error("404 Not Found: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Ressource non trouvée. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/clients");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                                log.error("500 Internal Server Error: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage",
                                                                     "Erreur interne de serveur. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createGarage");
                                return Mono.just("redirect:/error");
                            }
                            log.error("ERREUR: {}", e.getResponseBodyAsString());
                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                            redirectAttributes.addFlashAttribute("errorMessage", "Erreur non reconnue. " + e.getResponseBodyAsString());
                            redirectAttributes.addFlashAttribute("urlRedirection", "/clients");
                            return Mono.just("redirect:/error");
                        });
    }
    
}
