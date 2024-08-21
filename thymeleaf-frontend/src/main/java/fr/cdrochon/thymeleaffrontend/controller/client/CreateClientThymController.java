package fr.cdrochon.thymeleaffrontend.controller.client;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.ClientStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.PaysDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static fr.cdrochon.thymeleaffrontend.formatdata.ConvertObjectToJson.convertObjectToJson;

@Slf4j
@Controller
public class CreateClientThymController {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    private final WebClient webClient;
    private final RestClient restClient;
    
    public CreateClientThymController(WebClient webClient, RestClient restClient) {
        this.webClient = webClient;
        this.restClient = restClient;
    }
    
    /**
     * Affiche le formulaire de création d'un client et rempli les champs avec les données saisies par l'utilisateur s'il y a eu une erreur lors de la
     * validation du formulaire.
     *
     * @param model modèle du client: permet de passer des attributs à la vue
     * @return la vue createClientForm
     */
    @GetMapping("/createClient")
    public String createClient(Model model) {
        if(!model.containsAttribute("clientDTO")) {
            model.addAttribute("clientDTO", new ClientThymDTO());
        }
        model.addAttribute("clientStatuses", List.of(ClientStatusDTO.values()));
        model.addAttribute("paysList", List.of(PaysDTO.values()));
        model.addAttribute("valeurPaysParDefaut", PaysDTO.valeurPaysParDefaut());
        return "client/createClientForm";
    }
    
    /**
     * Création d'un client via un formulaire.
     *
     * @param clientDTO          le client à créer
     * @param result             le résultat de la validation du formulaire
     * @param redirectAttributes attributs de redirection
     * @param model              modèle du client: permet de passer des attributs à la vue
     * @return la vue clients si la création a réussi, sinon la vue createClientForm
     */
    @PostMapping(value = "/createClient")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<String> createClient(@Valid @ModelAttribute("clientDTO") ClientThymDTO clientDTO, BindingResult result, RedirectAttributes redirectAttributes,
                                     Model model) {
        if(result.hasErrors()) {
            result.getAllErrors().forEach(err -> log.error("LOG ERROR : {}", err.getDefaultMessage()));
            model.addAttribute("clientDTO", clientDTO);
            model.addAttribute("clientStatuses", List.of(ClientStatusDTO.values()));
            model.addAttribute("paysList", List.of(PaysDTO.values()));
            return Mono.just("client/createClientForm");
        }
        
        return webClient.post()
                        .uri("/commands/createClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(convertObjectToJson(clientDTO)) // convertit obj en JSON
                        .retrieve()
                        .bodyToMono(ClientThymDTO.class)// convertit en objet
                        .timeout(Duration.ofSeconds(5000))
                        .flatMap(clientThymDTO -> {
                            if(clientThymDTO == null) {
                                log.error("Erreur lors de la création du client");
                                return Mono.error(new RuntimeException("Erreur lors de la création du client"));
                            }
                            
                            redirectAttributes.addFlashAttribute("successMessage", "Client créé avec succès");
                            return Mono.just("redirect:/client/" + clientThymDTO.getId());
                            // redirection vers la liste des clients
                            //                            redirectAttributes.addFlashAttribute("successMessage", "Client créé avec succès");
                            //                            return Mono.just("redirect:/clients");
                        })
                        .onErrorResume(TimeoutException.class, e -> {
                            log.error("Timeout occurred: {}", e.getMessage());
                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                            redirectAttributes.addFlashAttribute("errorMessage", "Tempes de requête dépassé. Veuillez réessayer plus tard.");
                            redirectAttributes.addFlashAttribute("urlRedirection", "/createClient");
                            return Mono.just("redirect:/error");
                        })
                        .onErrorResume(WebClientResponseException.class, e -> {
                            if(e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                                log.error("400 Bad Request: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Requête invalide.");
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createClient");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                                log.error("401 Internal Server Error: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Accès non autorisé. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createClient");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.FORBIDDEN) {
                                log.error("403 Forbidden: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Accès interdit. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createClient");
                                return Mono.just("redirect:/error");
                                
                            } else if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                                log.error("404 Not Found: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Ressource non trouvée. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createClient");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.UNSUPPORTED_MEDIA_TYPE) {
                                log.error("415 Unsupported Media Type: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage",
                                                                     "Type de média non supporté. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createClient");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.LOCKED) {
                                log.error("423 Forbidden: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage",
                                                                     "Ressource verrouillée. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createClient");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                                log.error("500 Internal Server Error: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage",
                                                                     "Erreur interne de serveur. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createClient");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                                log.error("503 Internal Server Error: {}", e.getMessage());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Service indisponible : " + e.getMessage());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createClient");
                                return Mono.just("redirect:/error");
                            }
                            // reaffiche le formualire de création de client avec les données saisies par l'user
                            redirectAttributes.addFlashAttribute("clientDTO", clientDTO);
                            return Mono.just("redirect:/createClient");
                        });
    }
}
