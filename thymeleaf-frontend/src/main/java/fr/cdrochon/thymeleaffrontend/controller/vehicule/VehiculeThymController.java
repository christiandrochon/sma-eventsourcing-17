package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymConvertDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
public class VehiculeThymController {
    
    @Autowired
    private WebClient webClient;
    
    @GetMapping("/vehicule/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public Mono<String> getVehiculeByIdAsync(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        return webClient.get()
                        .uri("/queries/vehicules/" + id)
                        //                                  .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " +
                        //                                  getJwtTokenValue()))
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
    
    //    //    @PreAuthorize("hasAuthority('USER')")
    //    public String vehiculeById(@PathVariable String id, Model model) {
    //        VehiculeDateConvertDTO vehicule = restClient.get().uri(externalServiceUrl + "/queries/vehicules/" + id)
    //                                                    //                                      .headers(httpHeaders -> httpHeaders.set(HttpHeaders
    //                                                    //                                      .AUTHORIZATION, "Bearer " + getJwtTokenValue()))
    //                                                    .retrieve().body(new ParameterizedTypeReference<>() {
    //                });
    //        model.addAttribute("vehicule", vehicule);
    //        return "vehicule/view";
    //    }
    
    //TODO : rendre aync. Attention, lors du debug, la liste des vehicules n'est pas à jour lorsque je cree un nouveau vehicule. Mais en mlode normal, c'est ok
    @GetMapping("/vehicules")
    //    @PreAuthorize("hasAuthority('USER')")
    public Mono<String> getVehiculesAsync(Model model, RedirectAttributes redirectAttributes) {
        return webClient.get()
                        .uri("/queries/vehicules")
                        //                                         .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION,
                        //                                         "Bearer " + getJwtTokenValue()))
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
    //    public String vehicules(Model model) {
    //        List<VehiculeThymConvertDTO> vehicules = restClient.get().uri(externalServiceUrl + "/queries/vehicules")
    //                                                           //.headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " +
    //                                                           getJwtTokenValue()))
    //                                                           .retrieve().body(new ParameterizedTypeReference<>() {
    //                });
    //        model.addAttribute("vehicules", vehicules);
    //        return "vehicule/vehicules";
    //    }
}
