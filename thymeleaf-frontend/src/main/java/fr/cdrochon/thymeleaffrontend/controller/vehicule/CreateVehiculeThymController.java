package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymConvertDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymDTO;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static fr.cdrochon.thymeleaffrontend.json.ConvertObjectToJson.convertObjectToJson;
import static fr.cdrochon.thymeleaffrontend.security.SecurityConfigThymeleaf.getJwtTokenValue;

@Controller
@Slf4j
public class CreateVehiculeThymController {
    
    @Autowired
    private WebClient webClient;
    
    /**
     * Affiche le formulaire de création d'un vehicule
     *
     * @param model modèle du vehicule: permet de passer des attributs à la vue
     * @return la vue createVehiculeForm
     */
    @GetMapping("/createVehicule")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String createVehicule(Model model) {
        if(!model.containsAttribute("vehiculePostDTO")) {
            model.addAttribute("vehiculePostDTO", new VehiculeThymDTO());
        }
        //chargement des listes de type de document et de status de vehicule
        model.addAttribute("vehiculeStatuses", List.of(VehiculeStatusDTO.values()));
        return "vehicule/createVehiculeForm";
    }
    
    /***
     * Création d'un vehicule via un formulaire.
     * <p></p>
     * Vérifie si l'immatriculation existe déjà. Vérifie si une erreur est survenue lors de la création du vehicule. Vérifie si la date de mise en circulation
     * est valide. Vérifie si le status du vehicule est valide. Vérifie si le vehicule a bien été créé.
     *
     * @param vehiculePostDTO DTO du vehicule à créer
     * @param result BindingResult pour la validation du DTO
     * @param redirectAttributes attributs de redirection
     * @param model model de la vue
     * @return la vue de création d'un vehicule
     */
    @PostMapping(value = "/createVehicule")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<String> createDossierAsync(@Valid @ModelAttribute("vehiculePostDTO") VehiculeThymDTO vehiculePostDTO, BindingResult result,
                                           RedirectAttributes redirectAttributes, Model model) {
        if(result.hasErrors()) {
            log.error("LOG ERROR : {}", result.getAllErrors());
            model.addAttribute("vehiculePostDTO", vehiculePostDTO);
            model.addAttribute("vehiculeStatuses", List.of(VehiculeStatusDTO.values()));
            //ce return conserve l'etat du form et permet de reafficher les erreurs de validation courantes
            return Mono.just("vehicule/createVehiculeForm");
        }
        
        return immatriculationExiste(vehiculePostDTO.getImmatriculationVehicule())
                .flatMap(immatriculationExiste -> {
                    if(immatriculationExiste && vehiculePostDTO.getImmatriculationVehicule() != null) {
                        // Numéro d'immatriculation déjà existant
                        model.addAttribute("vehiculePostDTO", vehiculePostDTO);
                        model.addAttribute("vehiculeStatuses", List.of(VehiculeStatusDTO.values()));
                        model.addAttribute("immatriculationExisteError",
                                           "L'immatriculation existe déjà, vous ne pouvez pas créer deux véhicules ayant la même immatriculation.");
                        return Mono.just("vehicule/createVehiculeForm");
                    }
                    
                    
                    VehiculeThymConvertDTO vehiculeDateConvertDTO = convertVehiculeDTO(vehiculePostDTO);
                    String jsonPayload = convertObjectToJson(vehiculeDateConvertDTO);
                    log.info("JSON Payload: {}", jsonPayload);
                    
                    return webClient.post()
                                    .uri("/commands/createVehicule")
                                    .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .bodyValue(jsonPayload)
                                    .retrieve()
                                    .bodyToMono(VehiculeThymConvertDTO.class)
                                    .timeout(Duration.ofSeconds(300))
                                    .flatMap(vehicule -> {
                                        if(vehicule == null) {
                                            log.error("Erreur lors de la création du vehicule");
                                            return Mono.error(new RuntimeException("Erreur lors de la création du vehicule"));
                                        }
                                        
                                        log.info("Response: {}", vehicule);
                                        log.info("type de valeur : {}", vehicule.getDateMiseEnCirculationVehicule().getClass());
                                        log.info("Vehicule created successfully");
                                        redirectAttributes.addFlashAttribute("successMessage", "Vehicule créé avec succès");
                                        return Mono.just("redirect:/vehicules/" + vehicule.getId());
                                        // redirection vers la liste des clients
                                        //                            redirectAttributes.addFlashAttribute("successMessage", "Client créé avec succès");
                                        //                            return Mono.just("redirect:/dossier/" + dossier.getId());
                                    })
                                    .onErrorResume(TimeoutException.class, e -> {
                                        log.error("Timeout occurred: {}", e.getMessage());
                                        redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                        redirectAttributes.addFlashAttribute("errorMessage", "Tempes de requête dépassé. Veuillez réessayer plus tard.");
                                        redirectAttributes.addFlashAttribute("urlRedirection", "/createVehicule");
                                        return Mono.just("redirect:/error");
                                    })
                                    .onErrorResume(WebClientResponseException.class, e -> {
                                        if(e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                                            log.error("400 Bad Request: {}", e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                            redirectAttributes.addFlashAttribute("errorMessage", "Requête invalide.");
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createVehicule");
                                            return Mono.just("redirect:/error");
                                        } else if(e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                                            log.error("401 Internal Server Error: {}", e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                            redirectAttributes.addFlashAttribute("errorMessage", "Accès non autorisé. " + e
                                                    .getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createVehicule");
                                            return Mono.just("redirect:/error");
                                        } else if(e.getStatusCode() == HttpStatus.FORBIDDEN) {
                                            log.error("403 Forbidden: {}", e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                            redirectAttributes.addFlashAttribute("errorMessage", "Accès interdit. " + e
                                                    .getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createVehicule");
                                            return Mono.just("redirect:/error");
                                            
                                        } else if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                                            log.error("404 Not Found: {}", e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                            redirectAttributes.addFlashAttribute("errorMessage", "Ressource non trouvée. " + e
                                                    .getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createVehicule");
                                            return Mono.just("redirect:/error");
                                        } else if(e.getStatusCode() == HttpStatus.UNSUPPORTED_MEDIA_TYPE) {
                                            log.error("415 Unsupported Media Type: {}", e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                            redirectAttributes.addFlashAttribute("errorMessage",
                                                                                 "Type de média non supporté. " + e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createVehicule");
                                            return Mono.just("redirect:/error");
                                        } else if(e.getStatusCode() == HttpStatus.LOCKED) {
                                            log.error("423 Forbidden: {}", e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                            redirectAttributes.addFlashAttribute("errorMessage",
                                                                                 "Ressource verrouillée. " + e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createVehicule");
                                            return Mono.just("redirect:/error");
                                        } else if(e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                                            log.error("500 Internal Server Error: {}", e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                            redirectAttributes.addFlashAttribute("errorMessage",
                                                                                 "Erreur interne de serveur. " + e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createVehicule");
                                            return Mono.just("redirect:/error");
                                        } else if(e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                                            log.error("503 Internal Server Error: {}", e.getMessage());
                                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                            redirectAttributes.addFlashAttribute("errorMessage", "Service indisponible : " + e.getMessage());
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createVehicule");
                                            return Mono.just("redirect:/error");
                                        }
                                        log.error("Error: {}", e.getMessage());
                                        redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                        redirectAttributes.addFlashAttribute("errorMessage", "Service indisponible : " + e.getMessage());
                                        redirectAttributes.addFlashAttribute("urlRedirection", "/createVehicule");
                                        return Mono.just("redirect:/error");
                                    });
                });
        
    }
    
    /**
     * Convertit le DTO du vehicule qui pessede des attributs de type String en DTO du vehicule qui possede des attributs de type Instant.
     *
     * @param vehiculePostDTO DTO du vehicule à convertir
     * @return DTO du vehicule converti
     */
    private VehiculeThymConvertDTO convertVehiculeDTO(VehiculeThymDTO vehiculePostDTO) {
        
        try {
            
            //conversion du vehicule (à cause des dates Instant <> String)
            VehiculeThymConvertDTO vehiculeDateConvertDTO = new VehiculeThymConvertDTO();
            vehiculeDateConvertDTO.setId(vehiculePostDTO.getId());
            vehiculeDateConvertDTO.setImmatriculationVehicule(vehiculePostDTO.getImmatriculationVehicule());
            vehiculeDateConvertDTO.setVehiculeStatus(vehiculePostDTO.getVehiculeStatus());
            
            // conversion de la date de mise en circulation du vehicule
            //reconvertir la date de mise en circulation du véhicule en Instant , d'abord en LocalDate de type 'yyyy-MM-dd', puis réellement en Instant
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(vehiculePostDTO.getDateMiseEnCirculationVehicule(), formatter);
            Instant instant = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            vehiculeDateConvertDTO.setDateMiseEnCirculationVehicule(instant);
            
            
            return vehiculeDateConvertDTO;
        } catch(Exception e) {
            log.error("Erreur lors de la conversion du dossier: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la conversion du dossier: " + e.getMessage());
        }
    }
    
    /**
     * Empêche la création d'un vehicule si l'immatriculation existe déjà. Vérifie si un vehicule existe en fonction de son immatriculation.
     *
     * @param immatriculation immatriculation du vehicule
     * @return Boolean
     */
    private Mono<Boolean> immatriculationExiste(String immatriculation) {
        return webClient.get()
                        .uri("/queries/vehiculeExists/" + immatriculation)
                        .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(Boolean.class);
    }
}
