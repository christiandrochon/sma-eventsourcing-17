package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymConvertDTO;
import fr.cdrochon.thymeleaffrontend.logging.FrontendLoggers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Controller
public class VehiculeThymController {

    @Autowired
    private WebClient webClient;

    @GetMapping("/vehicule/{id}")
    public Mono<String> getVehiculeByIdAsync(@PathVariable String id, Model model) {
        return webClient.get()
                        .uri("/queries/vehicules/" + id)
                        .retrieve()
                        .bodyToMono(VehiculeThymConvertDTO.class)
                        .flatMap(dto -> {
                            assert dto != null;
                            model.addAttribute("vehicule", dto);
                            return Mono.just("vehicule/view");
                        })
                        .onErrorResume(WebClientResponseException.class, e -> {
                            FrontendLoggers.error().error("UI_VEHICULE_READ_FAILED status={} message={}", e.getStatusCode(), e.getResponseBodyAsString());
                            model.addAttribute("alertClass", "alert-danger");
                            model.addAttribute("urlRedirection", "/vehicules");
                            model.addAttribute("errorMessage", "Erreur lors de la récupération du véhicule (" + e.getStatusCode() + ").");
                            return Mono.just("error");
                        });
    }

    @GetMapping("/vehicules")
    public Mono<String> getVehiculesAsync(Model model) {
        return webClient.get()
                        .uri("/queries/vehicules")
                        .retrieve()
                        .bodyToFlux(VehiculeThymConvertDTO.class)
                        .collectList()
                        .flatMap(vehicules -> {
                            assert vehicules != null;
                            model.addAttribute("vehicules", vehicules);
                            return Mono.just("vehicule/vehicules");
                        })
                        .onErrorResume(WebClientResponseException.class, e -> {
                            FrontendLoggers.error().error("UI_VEHICULE_LIST_FAILED status={} message={}", e.getStatusCode(), e.getResponseBodyAsString());
                            model.addAttribute("alertClass", "alert-danger");
                            model.addAttribute("urlRedirection", "/vehicules");
                            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                                model.addAttribute("errorMessage", "Accès interdit : vous n'avez pas les droits pour consulter les véhicules.");
                            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                                model.addAttribute("errorMessage", "Non authentifié : veuillez vous reconnecter.");
                            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                                model.addAttribute("errorMessage", "Aucun véhicule trouvé.");
                            } else {
                                model.addAttribute("errorMessage", "Erreur inattendue (" + e.getStatusCode() + ").");
                            }
                            return Mono.just("error");
                        })
                        .onErrorResume(Exception.class, e -> {
                            FrontendLoggers.error().error("UI_VEHICULE_LIST_UNEXPECTED_ERROR message={}", e.getMessage(), e);
                            model.addAttribute("alertClass", "alert-danger");
                            model.addAttribute("urlRedirection", "/vehicules");
                            model.addAttribute("errorMessage", "Erreur de connexion au serveur : " + e.getMessage());
                            return Mono.just("error");
                        });
    }
}
