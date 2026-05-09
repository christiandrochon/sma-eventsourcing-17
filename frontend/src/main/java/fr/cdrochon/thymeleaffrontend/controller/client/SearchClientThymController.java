package fr.cdrochon.thymeleaffrontend.controller.client;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymConvertDTO;
import fr.cdrochon.thymeleaffrontend.logging.FrontendLoggers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

@Controller
public class SearchClientThymController {

    @Autowired
    private WebClient webClient;

    /**
     * Fournit un objet ClientThymConvertDTO vide au modèle Thymeleaf.
     * Permet à l'IDE de résoudre le type lié à th:object="${clientPostDTO}".
     */
    @ModelAttribute("clientPostDTO")
    public ClientThymConvertDTO clientPostDTO() {
        return new ClientThymConvertDTO();
    }

    /**
     * Affiche l'ensemble des clients dans la liste déroulante du formulaire de recherche de client.
     * <p>
     * Preference d'utiliser l'approche asynchrone pour la fluidité de l'application
     *
     * @param model              model de la vue
     * @param redirectAttributes attributs de redirection
     * @return la vue client/searchClientForm
     */
    @GetMapping(value = "/searchclient")
    public Mono<String> searchClientsAsync(Model model, RedirectAttributes redirectAttributes) {
        return webClient.get()
                        .uri("/queries/clients")
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToFlux(ClientThymConvertDTO.class)
                        .collectList()
                        .flatMap(clients -> {
                            assert clients != null;
                            model.addAttribute("clients", clients);
                            model.addAttribute("clientPostDTO", new ClientThymConvertDTO());
                            return Mono.just("client/searchClientForm");
                        })
                        .onErrorResume(WebClientResponseException.class, e -> {
                            FrontendLoggers.error().error("UI_CLIENT_SEARCH_FAILED status={} message={}", e.getStatusCode().value(), e.getResponseBodyAsString());
                            if(e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Requête invalide.");
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createGarage");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Ressource non trouvée. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/clients");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Erreur interne de serveur. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createGarage");
                                return Mono.just("redirect:/error");
                            }
                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                            redirectAttributes.addFlashAttribute("errorMessage", "Erreur non reconnue. " + e.getResponseBodyAsString());
                            redirectAttributes.addFlashAttribute("urlRedirection", "/clients");
                            return Mono.just("redirect:/error");
                        });
    }

    /**
     * Recherche un client par son id et affiche ses informations.
     * <p>
     * Preference d'utiliser l'approche asynchrone pour la fluidité de l'application
     *
     * @param id    id du client
     * @param model model de la vue
     * @return la vue client/view
     */
    @GetMapping(value = "/searchclient/{id}")
    public Mono<String> searchClientByIdAsync(@PathVariable String id, Model model) {
        return webClient.get()
                        .uri("/queries/clients/" + id)
                        .retrieve()
                        .bodyToMono(ClientThymConvertDTO.class)
                        .onErrorResume(throwable -> Mono.error(new RuntimeException("Erreur lors de la récupération du client")))
                        .flatMap(clientThymDTO -> {
                            model.addAttribute("client", clientThymDTO);
                            return Mono.just("client/view");
                        });
    }
}
