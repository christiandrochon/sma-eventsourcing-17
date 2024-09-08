package fr.cdrochon.thymeleaffrontend.controller.garage;

import fr.cdrochon.thymeleaffrontend.dtos.garage.GarageAdresseDTO;
import fr.cdrochon.thymeleaffrontend.dtos.garage.GaragePostDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static fr.cdrochon.thymeleaffrontend.json.ConvertObjectToJson.convertObjectToJson;
import static fr.cdrochon.thymeleaffrontend.security.SecurityConfigThymeleaf.getJwtTokenValue;

@Controller
@Slf4j
public class CreateGarageThymController {
    
    @Autowired
    private WebClient webClient;
    
    /**
     * Affiche le formulaire de création d'un garage et recharge le formulaire avec les données déjà saisies en cas d'erreur
     *
     * @param model model de la vue
     * @return la vue garage/createGarageForm
     */
    @GetMapping("/createGarage")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarageAsync(Model model) {
        GaragePostDTO garageDTO = new GaragePostDTO();
        garageDTO.setAdresse(new GarageAdresseDTO());
        
        //rafraichissement de l'ecran
        if(!model.containsAttribute("garageDTO")) {
            model.addAttribute("garageDTO", new GaragePostDTO());
        }
        return "garage/createGarageForm";
    }
    
    /**
     * Crée un garage
     *
     * @param garageDTO          DTO contenant les informations du garage à créer
     * @param result             BindingResult pour les erreurs de validation
     * @param redirectAttributes attributs de redirection pour les messages d'erreur
     * @param model              model de la vue pour les données
     * @return la vue garage/createGarageForm en cas d'erreur, la vue garage/view en cas de succès
     */
    @PostMapping(path = "/createGarage")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<String> createGarageAsync(@Valid @ModelAttribute("garageDTO") GaragePostDTO garageDTO, BindingResult result,
                                          RedirectAttributes redirectAttributes, Model model) {
        if(result.hasErrors()) {
            model.addAttribute("garageDTO", garageDTO);
            result.getAllErrors().forEach(err -> log.error("LOG ERROR : {}", err.getDefaultMessage()));
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + result.getAllErrors().get(0).getDefaultMessage());
            return Mono.just("garage/createGarageForm");
        }
        //CHECKME : vérifier si le rafraichissement de la liste des garages est nécessaire
        return webClient.post()
                        .uri("/commands/createGarage")
                        .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(convertObjectToJson(garageDTO)) // convertit garage en JSON
                        .retrieve()
                        .bodyToMono(GaragePostDTO.class)// convertit en objet
                        .timeout(Duration.ofSeconds(30))
                        .flatMap(garagePostDTO -> {
                            if(garagePostDTO == null) {
                                log.error("Erreur lors de la création du garage");
                                return Mono.error(new RuntimeException("Erreur lors de la création du garage"));
                            }
                            
                            redirectAttributes.addFlashAttribute("successMessage", "Garage créé avec succès");
                            return Mono.just("redirect:/garage/" + garagePostDTO.getId());
                            // redirection vers la liste des garages
                            //                            redirectAttributes.addFlashAttribute("successMessage", "Garage créé avec succès");
                            //                            return Mono.just("redirect:/garages");
                        })
                        .onErrorResume(TimeoutException.class, e -> {
                            log.error("Timeout occurred: {}", e.getMessage());
                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                            redirectAttributes.addFlashAttribute("errorMessage", "Tempes de requête dépassé. Veuillez réessayer plus tard.");
                            redirectAttributes.addFlashAttribute("urlRedirection", "/createGarage");
                            return Mono.just("redirect:/error");
                        })
                        .onErrorResume(WebClientResponseException.class, e -> {
                            if(e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                                log.error("400 Bad Request: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Requête invalide.");
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createGarage");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                                log.error("401 Internal Server Error: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Accès non autorisé. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createGarage");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.FORBIDDEN) {
                                log.error("403 Forbidden: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Accès interdit. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createGarage");
                                return Mono.just("redirect:/error");
                                
                            } else if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                                log.error("404 Not Found: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Ressource non trouvée. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createGarage");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.UNSUPPORTED_MEDIA_TYPE) {
                                log.error("415 Unsupported Media Type: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage",
                                                                     "Type de média non supporté. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createGarage");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.LOCKED) {
                                log.error("423 Forbidden: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage",
                                                                     "Ressource verrouillée. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createGarage");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                                log.error("500 Internal Server Error: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage",
                                                                     "Erreur interne de serveur. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createGarage");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                                log.error("503 Internal Server Error: {}", e.getMessage());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Service indisponible : " + e.getMessage());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createGarage");
                                return Mono.just("redirect:/error");
                            }
                            // reaffiche le formualire de création de garage avec les données saisies par l'user
                            redirectAttributes.addFlashAttribute("garageDTO", garageDTO);
                            return Mono.just("redirect:/createGarage");
                        });
    }
}
