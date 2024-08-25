package fr.cdrochon.thymeleaffrontend.controller.dossier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.cdrochon.thymeleaffrontend.dtos.client.ClientStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.PaysDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierStatusThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierThymConvertDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeDateConvertDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import static fr.cdrochon.thymeleaffrontend.formatdata.ConvertObjectToJson.convertObjectToJson;

@Controller
@Slf4j
public class CreateDossierThymController {
    
    @Autowired
    private WebClient webClient;
    //    @Autowired
    //    private RestClient restClient;
    
    /**
     * Affiche le formulaire de création d'un dossier
     *
     * @param model modèle du dossier: permet de passer des attributs à la vue
     * @return la vue createDossierForm
     */
    @GetMapping("/createDossier")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String getDossier(Model model) {
        if(!model.containsAttribute("dossierDTO")) {
            model.addAttribute("dossierDTO", new DossierThymDTO());
        }
        
        //chargement des listes de status de dossier, de status de vehicule, de status de client et de pays
        //        modelAttributesError(model);
        model.addAttribute("dossierStatuses", List.of(DossierStatusThymDTO.values()));
        model.addAttribute("vehiculeStatuses", List.of(VehiculeStatusDTO.values()));
        model.addAttribute("clientStatuses", List.of(ClientStatusDTO.values()));
        model.addAttribute("paysList", List.of(PaysDTO.values()));
        model.addAttribute("valeurClientStatutParDefaut", ClientStatusDTO.valeurClientStatutParDefaut());
        model.addAttribute("valeurDossierStatutParDefaut", DossierStatusThymDTO.valeurDossierStatutParDefaut());
        model.addAttribute("valeurPaysParDefaut", PaysDTO.valeurPaysParDefaut());
        return "dossier/createDossierForm";
    }
    
    /**
     * Crée un dossier
     *
     * @param dossierThymDTO     dto du dossier à créer
     * @param result             résultat de la validation
     * @param redirectAttributes attributs de redirection
     * @param model              modèle de la vue
     * @return la vue createDossierForm si erreur, la vue dossier/{id} si succès
     */
    @PostMapping(value = "/createDossier")
    public Mono<String> createDossierAsync(@Valid @ModelAttribute("dossierDTO") DossierThymDTO dossierThymDTO, BindingResult result,
                                           RedirectAttributes redirectAttributes, Model model) {
        if(result.hasErrors()) {
            result.getAllErrors().forEach(err -> log.error("LOG ERROR : {}", err.getDefaultMessage()));
            model.addAttribute("dossierDTO", dossierThymDTO);
            modelAttributesError(model);
            return Mono.just("dossier/createDossierForm");
        }
        
        return immatriculationExiste(dossierThymDTO.getVehicule().getImmatriculationVehicule())
                .flatMap(exists -> {
                    if(exists) {
                        model.addAttribute("dossierDTO", dossierThymDTO);
                        modelAttributesError(model);
                        model.addAttribute("immatriculationExisteError",
                                           "L'immatriculation existe déjà, vous ne pouvez pas créer deux véhicules ayant la même immatriculation.");
                        return Mono.just("dossier/createDossierForm");
                    }
                    
                    DossierThymConvertDTO dossierConvertThymDTO = convertDossierDTO(dossierThymDTO);
                    String jsonPayload = convertObjectToJson(dossierConvertThymDTO);
                    log.info("JSON Payload: {}", jsonPayload);
                    
                    return webClient.post()
                                    .uri("/commands/createDossier")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .bodyValue(jsonPayload)
                                    .retrieve()
                                    .bodyToMono(DossierThymConvertDTO.class)
                                    .timeout(Duration.ofSeconds(3000))
                                    .flatMap(dossier -> {
                                        if(dossier == null) {
                                            log.error("Erreur lors de la création du dossier");
                                            return Mono.error(new RuntimeException("Erreur lors de la création du dossier"));
                                        }
                                        
                                        log.info("Response: {}", dossier);
                                        log.info("type de valeur : {}", dossier.getDateCreationDossier().getClass());
                                        log.info("Dossier created successfully");
                                        redirectAttributes.addFlashAttribute("successMessage", "Dossier créé avec succès");
                                        return Mono.just("redirect:/dossier/" + dossier.getId());
                                        // redirection vers la liste des clients
                                        //                            redirectAttributes.addFlashAttribute("successMessage", "Client créé avec succès");
                                        //                            return Mono.just("redirect:/dossier/" + dossier.getId());
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
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
                                            return Mono.just("redirect:/error");
                                        } else if(e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                                            log.error("401 Internal Server Error: {}", e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                            redirectAttributes.addFlashAttribute("errorMessage", "Accès non autorisé. " + e
                                                    .getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
                                            return Mono.just("redirect:/error");
                                        } else if(e.getStatusCode() == HttpStatus.FORBIDDEN) {
                                            log.error("403 Forbidden: {}", e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                            redirectAttributes.addFlashAttribute("errorMessage", "Accès interdit. " + e
                                                    .getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
                                            return Mono.just("redirect:/error");
                                            
                                        } else if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                                            log.error("404 Not Found: {}", e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                            redirectAttributes.addFlashAttribute("errorMessage", "Ressource non trouvée. " + e
                                                    .getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
                                            return Mono.just("redirect:/error");
                                        } else if(e.getStatusCode() == HttpStatus.UNSUPPORTED_MEDIA_TYPE) {
                                            log.error("415 Unsupported Media Type: {}", e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                            redirectAttributes.addFlashAttribute("errorMessage",
                                                                                 "Type de média non supporté. " + e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
                                            return Mono.just("redirect:/error");
                                        } else if(e.getStatusCode() == HttpStatus.LOCKED) {
                                            log.error("423 Forbidden: {}", e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                            redirectAttributes.addFlashAttribute("errorMessage",
                                                                                 "Ressource verrouillée. " + e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
                                            return Mono.just("redirect:/error");
                                        } else if(e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                                            log.error("500 Internal Server Error: {}", e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                            redirectAttributes.addFlashAttribute("errorMessage",
                                                                                 "Erreur interne de serveur. " + e.getResponseBodyAsString());
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
                                            return Mono.just("redirect:/error");
                                        } else if(e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                                            log.error("503 Internal Server Error: {}", e.getMessage());
                                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                            redirectAttributes.addFlashAttribute("errorMessage", "Service indisponible : " + e.getMessage());
                                            redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
                                            return Mono.just("redirect:/error");
                                        }
                                        //                                             handleWebClientResponseException(e, redirectAttributes,
                                        //                                             dossierThymDTO);
                                        // reaffiche le formualire de création de client avec les données saisies par l'user
                                        redirectAttributes.addFlashAttribute("dossierDTO", dossierThymDTO);
                                        return Mono.just("redirect:/createDossier");
                                    });
                });
    }
    //    @PostMapping(value = "/createDossier")
    //    //    @PreAuthorize("hasAuthority('ADMIN')")
    //    public Mono<String> createDossierAsync(@Valid @ModelAttribute("dossierDTO") DossierThymDTO dossierThymDTO, BindingResult result,
    //                                           RedirectAttributes redirectAttributes,
    //                                           Model model) {
    //        if(result.hasErrors()) {
    //            result.getAllErrors().forEach(err -> log.error("LOG ERROR : {}", err.getDefaultMessage()));
    //            model.addAttribute("dossierDTO", dossierThymDTO);
    //            modelAttributesError(model);
    //            //                        modelAttributesError(dossierThymDTO, model);
    //            return Mono.just("dossier/createDossierForm");
    //        }
    //        // Numéro d'immatriculation déjà existant
    //        if(immatriculationExiste(dossierThymDTO.getVehicule().getImmatriculationVehicule())
    //                && dossierThymDTO.getVehicule().getImmatriculationVehicule() != null) {
    //            //                        modelAttributesError(dossierThymDTO, model);
    //            model.addAttribute("dossierDTO", dossierThymDTO);
    //            modelAttributesError(model);
    //            model.addAttribute("immatriculationExisteError",
    //                               "L'immatriculation existe déjà, vous ne pouvez pas créer deux véhicules ayant la même immatriculation.");
    //            return Mono.just("dossier/createDossierForm");
    //        }
    //
    //        DossierThymConvertDTO dossierConvertThymDTO = convertDossierDTO(dossierThymDTO);
    //        String jsonPayload = convertObjectToJson(dossierConvertThymDTO);
    //        log.info("JSON Payload: {}", jsonPayload);
    //
    //        return webClient.post()
    //                        .uri("/commands/createDossier")
    //                        .contentType(MediaType.APPLICATION_JSON)
    //                        .accept(MediaType.APPLICATION_JSON)
    //                        .bodyValue(jsonPayload)
    //                        //                        .bodyValue(convertObjectToJson(dossierConvertThymDTO)) // convertit obj en JSON
    //                        .retrieve()
    //                        .bodyToMono(DossierThymConvertDTO.class)// convertit en objet
    //                        .timeout(Duration.ofSeconds(5000))
    //                        .flatMap(dossier -> {
    //                            if(dossier == null) {
    //                                log.error("Erreur lors de la création du dossier");
    //                                return Mono.error(new RuntimeException("Erreur lors de la création du dossier"));
    //                            }
    //                            log.info("Response: {}", dossier);
    //                            log.info("Dossier created successfully");
    //                            redirectAttributes.addFlashAttribute("successMessage", "Dossier créé avec succès");
    //                            return Mono.just("redirect:/dossier/" + dossier.getId());
    //                            // redirection vers la liste des clients
    //                            //                            redirectAttributes.addFlashAttribute("successMessage", "Client créé avec succès");
    //                            //                            return Mono.just("redirect:/dossier/" + dossier.getId());
    //                        })
    //                        .onErrorResume(TimeoutException.class, e -> {
    //                            log.error("Timeout occurred: {}", e.getMessage());
    //                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
    //                            redirectAttributes.addFlashAttribute("errorMessage", "Tempes de requête dépassé. Veuillez réessayer plus tard.");
    //                            redirectAttributes.addFlashAttribute("urlRedirection", "/createClient");
    //                            return Mono.just("redirect:/error");
    //                        })
    //                        .onErrorResume(WebClientResponseException.class, e -> {
    //                            if(e.getStatusCode() == HttpStatus.BAD_REQUEST) {
    //                                log.error("400 Bad Request: {}", e.getResponseBodyAsString());
    //                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
    //                                redirectAttributes.addFlashAttribute("errorMessage", "Requête invalide.");
    //                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
    //                                return Mono.just("redirect:/error");
    //                            } else if(e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
    //                                log.error("401 Internal Server Error: {}", e.getResponseBodyAsString());
    //                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
    //                                redirectAttributes.addFlashAttribute("errorMessage", "Accès non autorisé. " + e
    //                                        .getResponseBodyAsString());
    //                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
    //                                return Mono.just("redirect:/error");
    //                            } else if(e.getStatusCode() == HttpStatus.FORBIDDEN) {
    //                                log.error("403 Forbidden: {}", e.getResponseBodyAsString());
    //                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
    //                                redirectAttributes.addFlashAttribute("errorMessage", "Accès interdit. " + e
    //                                        .getResponseBodyAsString());
    //                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
    //                                return Mono.just("redirect:/error");
    //
    //                            } else if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
    //                                log.error("404 Not Found: {}", e.getResponseBodyAsString());
    //                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
    //                                redirectAttributes.addFlashAttribute("errorMessage", "Ressource non trouvée. " + e
    //                                        .getResponseBodyAsString());
    //                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
    //                                return Mono.just("redirect:/error");
    //                            } else if(e.getStatusCode() == HttpStatus.UNSUPPORTED_MEDIA_TYPE) {
    //                                log.error("415 Unsupported Media Type: {}", e.getResponseBodyAsString());
    //                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
    //                                redirectAttributes.addFlashAttribute("errorMessage",
    //                                                                     "Type de média non supporté. " + e.getResponseBodyAsString());
    //                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
    //                                return Mono.just("redirect:/error");
    //                            } else if(e.getStatusCode() == HttpStatus.LOCKED) {
    //                                log.error("423 Forbidden: {}", e.getResponseBodyAsString());
    //                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
    //                                redirectAttributes.addFlashAttribute("errorMessage",
    //                                                                     "Ressource verrouillée. " + e.getResponseBodyAsString());
    //                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
    //                                return Mono.just("redirect:/error");
    //                            } else if(e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
    //                                log.error("500 Internal Server Error: {}", e.getResponseBodyAsString());
    //                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
    //                                redirectAttributes.addFlashAttribute("errorMessage",
    //                                                                     "Erreur interne de serveur. " + e.getResponseBodyAsString());
    //                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
    //                                return Mono.just("redirect:/error");
    //                            } else if(e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
    //                                log.error("503 Internal Server Error: {}", e.getMessage());
    //                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
    //                                redirectAttributes.addFlashAttribute("errorMessage", "Service indisponible : " + e.getMessage());
    //                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
    //                                return Mono.just("redirect:/error");
    //                            }
    //                            //                                             handleWebClientResponseException(e, redirectAttributes,
    //                            //                                             dossierThymDTO);
    //                            // reaffiche le formualire de création de client avec les données saisies par l'user
    //                            redirectAttributes.addFlashAttribute("dossierDTO", dossierThymDTO);
    //                            return Mono.just("redirect:/createDossier");
    //                        });
    //    }
    
    private DossierThymDTO convertJsonToDossierThymDTO(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            log.info("JSON Response: {}", jsonResponse);
            return objectMapper.readValue(jsonResponse, DossierThymDTO.class);
        } catch(JsonProcessingException e) {
            log.error("Error converting JSON response to DossierThymDTO: {}", e.getMessage());
            return null;
        }
    }
    
    private void handleWebClientResponseException(WebClientResponseException e, RedirectAttributes redirectAttributes, DossierThymDTO dossierThymDTO) {
        //        HttpStatus status = e.getStatus();
        String responseBody = e.getResponseBodyAsString();
        //        log.error("{}: {}", statusCode, responseBody);
        redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        redirectAttributes.addFlashAttribute("errorMessage", responseBody);
        redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
        redirectAttributes.addFlashAttribute("dossierDTO", dossierThymDTO);
    }
    
    /**
     * Ajoute les attributs du modèle pour les erreurs de validation.
     *
     * @param model modèle de la vue
     */
    private void modelAttributesError(Model model) {
        model.addAttribute("dossierStatuses", List.of(DossierStatusThymDTO.values()));
        model.addAttribute("vehiculeStatuses", List.of(VehiculeStatusDTO.values()));
        model.addAttribute("clientStatuses", List.of(ClientStatusDTO.values()));
        model.addAttribute("valeurDossierStatutParDefaut", DossierStatusThymDTO.valeurDossierStatutParDefaut());
        model.addAttribute("paysList", List.of(PaysDTO.values()));
        model.addAttribute("valeurPaysParDefaut", PaysDTO.valeurPaysParDefaut());
        model.addAttribute("valeurClientStatutParDefaut", ClientStatusDTO.valeurClientStatutParDefaut());
    }
    
    
    //        private void modelAttributesError(@ModelAttribute("dossierDTO") @Valid DossierThymDTO dossierPostDTO, Model model) {
    //            model.addAttribute("dossierStatuses", List.of(DossierStatusDTO.values()));
    //            model.addAttribute("vehiculeStatuses", List.of(VehiculeStatusDTO.values()));
    //            model.addAttribute("clientStatuses", List.of(ClientStatusDTO.values()));
    //            model.addAttribute("paysList", List.of(PaysDTO.values()));
    //            model.addAttribute("dossierDTO", dossierPostDTO);
    //        }
    
    //    @PostMapping(value = "/createDossier")
    //    //    @PreAuthorize("hasAuthority('ADMIN')")
    //    public String createDocument(@Valid @ModelAttribute("dossierDTO") DossierThymDTO dossierPostDTO, BindingResult result,
    //                                 RedirectAttributes redirectAttributes, Model model) {
    //        if(result.hasErrors()) {
    //            result.getAllErrors().forEach(err -> log.error("LOG ERROR : {}", err.getDefaultMessage()));
    //            modelAttributesError(dossierPostDTO, model);
    //            //ce return conserve l'etat du form et permet de reafficher les erreurs de validation courantes
    //            return "dossier/createDossierForm";
    //        }
    //        try {
    //            if(immatriculationExiste(dossierPostDTO.getVehicule().getImmatriculationVehicule()) && dossierPostDTO.getVehicule()
    //                                                                                                                 .getImmatriculationVehicule() != null) {
    //                // Numéro d'immatriculation déjà existant
    //                modelAttributesError(dossierPostDTO, model);
    //                model.addAttribute("immatriculationExisteError",
    //                                   "L'immatriculation existe déjà, vous ne pouvez pas créer deux véhicules ayant la même immatriculation.");
    //                return "dossier/createDossierForm";
    //            }
    //
    //            //conversion du vehciuel à cause la date de mise en circulation
    //            VehiculeDateConvertDTO vehiculeDateConvertDTO = new VehiculeDateConvertDTO();
    //            vehiculeDateConvertDTO.setImmatriculationVehicule(dossierPostDTO.getVehicule().getImmatriculationVehicule());
    //            vehiculeDateConvertDTO.setVehiculeStatus(dossierPostDTO.getVehicule().getVehiculeStatus());
    //            // conversion de la date de mise en circulation du vehicule
    //            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    //            String dateMiseEnCirculationVehicule = dossierPostDTO.getVehicule().getDateMiseEnCirculationVehicule();
    //            LocalDate localDate = LocalDate.parse(dateMiseEnCirculationVehicule, formatter);
    //            Instant date = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    //            vehiculeDateConvertDTO.setDateMiseEnCirculationVehicule(date);
    //
    //            //conversion du dossier
    //            DossierConvertPostDTO dossierConvertPostDTO = new DossierConvertPostDTO();
    //            dossierConvertPostDTO.setNomDossier(dossierPostDTO.getNomDossier());
    //            dossierConvertPostDTO.setDateCreationDossier(Instant.now());
    //            dossierConvertPostDTO.setDateModificationDossier(Instant.now());
    //            dossierConvertPostDTO.setDossierStatus(dossierPostDTO.getDossierStatus());
    //            dossierConvertPostDTO.setClient(dossierPostDTO.getClient());
    //            dossierConvertPostDTO.setVehicule(vehiculeDateConvertDTO);
    //
    //            //appel du ms dossier pour la création du dossier
    //            webClient.post()
    //                     .uri(externalServiceUrl + "/commands/createDossier")
    //                     .contentType(MediaType.APPLICATION_JSON)
    //                     .bodyValue(dossierConvertPostDTO)
    //                     .retrieve()
    //                     .bodyToMono(String.class)
    //                     .block();
    //
    //            log.info("Dossier created successfully");
    //            redirectAttributes.addFlashAttribute("successMessage", "Dossier créé avec succès");
    //            redirectAttributes.addFlashAttribute("urlRedirection", "/dossiers");
    //            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
    //            return "redirect:/success";
    //            //            return "redirect:/dossiers";
    //
    //        } catch(Exception e) {
    //            //redirect : on passe ue url, principalement utilisées après un succès (POST-REDIRECT-GET) pour éviter la répétition de soumissions du
    //            formulaire
    //            redirectAttributes.addFlashAttribute("errorMessage",
    //                                                 "Erreur de création de dossier. Merci de communiquer le contenu de l'erreur suivante au développeur :
    //                                                 '" + e.getMessage() + "'");
    //            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
    //            redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
    //            redirectAttributes.addFlashAttribute("dossierDTO", dossierPostDTO);
    //            //            return "redirect:/createDossier";
    //            return "redirect:/error";
    //
    //        }
    //    }
    
    /**
     * Conversion de DossierThymDTO en DossierThymConvertDTO
     * <p></p>
     * Convertit le dto en un autre dto qui convertit la valeurs des String recus en json en un objet qui prend en charge la classe Instant
     *
     * @param dossierThymDTO dto converti avec les valeurs de date en type Instant
     * @return DossierThymConvertDTO
     */
    private DossierThymConvertDTO convertDossierDTO(DossierThymDTO dossierThymDTO) {
       
        try {
            
            //conversion du vehciuel à cause la date de mise en circulation
            VehiculeDateConvertDTO vehiculeDateConvertDTO = new VehiculeDateConvertDTO();
            vehiculeDateConvertDTO.setIdVehicule(dossierThymDTO.getVehicule().getIdVehicule());
            vehiculeDateConvertDTO.setImmatriculationVehicule(dossierThymDTO.getVehicule().getImmatriculationVehicule());
            vehiculeDateConvertDTO.setVehiculeStatus(dossierThymDTO.getVehicule().getVehiculeStatus());
            // conversion de la date de mise en circulation du vehicule
            //reconvertir la date de mise en circulation du véhicule en Instant , d'abord en LocalDate de type 'yyyy-MM-dd', puis réellement en Instant
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(dossierThymDTO.getVehicule().getDateMiseEnCirculationVehicule(), formatter);
            Instant instant = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            //        String dateMiseEnCirculationVehicule = dossierThymDTO.getVehicule().getDateMiseEnCirculationVehicule();
            //        LocalDate localDate = LocalDate.parse(dateMiseEnCirculationVehicule, formatter);
            //        Instant date = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);

            vehiculeDateConvertDTO.setDateMiseEnCirculationVehicule(instant);
            
            // mapper du client
            ClientThymDTO clientThymDTO = new ClientThymDTO();
            clientThymDTO.setId(dossierThymDTO.getClient().getId());
            clientThymDTO.setNomClient(dossierThymDTO.getClient().getNomClient());
            clientThymDTO.setPrenomClient(dossierThymDTO.getClient().getPrenomClient());
            clientThymDTO.setMailClient(dossierThymDTO.getClient().getMailClient());
            clientThymDTO.setTelClient(dossierThymDTO.getClient().getTelClient());
            clientThymDTO.setAdresse(dossierThymDTO.getClient().getAdresse());
            clientThymDTO.setClientStatus(dossierThymDTO.getClient().getClientStatus());
            
            //mapper du dossier
            DossierThymConvertDTO dossierConvertPostDTO = new DossierThymConvertDTO();
            dossierConvertPostDTO.setId(dossierThymDTO.getId());
            dossierConvertPostDTO.setNomDossier(dossierThymDTO.getNomDossier());
            dossierConvertPostDTO.setDossierStatus(dossierThymDTO.getDossierStatus());
            dossierConvertPostDTO.setClient(clientThymDTO);
            dossierConvertPostDTO.setVehicule(vehiculeDateConvertDTO);
            
            dossierConvertPostDTO.setDateCreationDossier(Instant.now());
            dossierConvertPostDTO.setDateModificationDossier(Instant.now());
            
            //        //conversion de la date de creation du dossier
            //        String dateCreationDossier = dossierThymDTO.getDateCreationDossier();
            //        LocalDate localDateCreationDossier = LocalDate.parse(dateCreationDossier, formatter);
            //        Instant dateCreation = localDateCreationDossier.atStartOfDay().toInstant(ZoneOffset.UTC);
            //        dossierConvertPostDTO.setDateCreationDossier(dateCreation);
            //        //conversion de la date de modification du dossier
            //        String dateModificationDossier = dossierThymDTO.getDateModificationDossier();
            //        LocalDate localDateModificationDossier = LocalDate.parse(dateModificationDossier, formatter);
            //        Instant dateModification = localDateModificationDossier.atStartOfDay().toInstant(ZoneOffset.UTC);
            //        dossierConvertPostDTO.setDateModificationDossier(dateModification);
            
            
            return dossierConvertPostDTO;
        } catch(Exception e) {
            log.error("Erreur lors de la conversion du dossier: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la conversion du dossier: " + e.getMessage());
        }
    }
    
    /**
     * Convertion de DossierThymConvertDTO en DossierThymDTO
     *
     * @param dossierConvertDTO DossierThymConvertDTO
     * @return DossierThymDTO
     */
    private DossierThymDTO convertToDossierThymDto(DossierThymConvertDTO dossierConvertDTO) {
        
        VehiculeThymDTO vehiculeThymDTO = new VehiculeThymDTO();
        vehiculeThymDTO.setIdVehicule(dossierConvertDTO.getVehicule().getIdVehicule());
        vehiculeThymDTO.setImmatriculationVehicule(dossierConvertDTO.getVehicule().getImmatriculationVehicule());
        vehiculeThymDTO.setVehiculeStatus(dossierConvertDTO.getVehicule().getVehiculeStatus());
        //date vehicule date de mise en circulation
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        Instant dateMiseEnCirculationVehicule = dossierConvertDTO.getVehicule().getDateMiseEnCirculationVehicule();
        String localDate = dateMiseEnCirculationVehicule.atOffset(ZoneOffset.UTC).toLocalDate().format(formatter);
        vehiculeThymDTO.setDateMiseEnCirculationVehicule(localDate);
        
        ClientThymDTO clientThymDTO = new ClientThymDTO();
        clientThymDTO.setId(dossierConvertDTO.getClient().getId());
        clientThymDTO.setNomClient(dossierConvertDTO.getClient().getNomClient());
        clientThymDTO.setPrenomClient(dossierConvertDTO.getClient().getPrenomClient());
        clientThymDTO.setMailClient(dossierConvertDTO.getClient().getMailClient());
        clientThymDTO.setTelClient(dossierConvertDTO.getClient().getTelClient());
        clientThymDTO.setAdresse(dossierConvertDTO.getClient().getAdresse());
        clientThymDTO.setClientStatus(dossierConvertDTO.getClient().getClientStatus());
        
        
        DossierThymDTO dossierThymDTO = new DossierThymDTO();
        dossierThymDTO.setId(dossierConvertDTO.getId());
        dossierThymDTO.setNomDossier(dossierConvertDTO.getNomDossier());
        
        //date de creation du dossier
        Instant dateCreationDossier = dossierConvertDTO.getDateCreationDossier();
        String dateCreationDossierLocale = dateCreationDossier.atOffset(ZoneOffset.UTC).toLocalDate().format(formatter);
        dossierThymDTO.setDateCreationDossier(dateCreationDossierLocale);
        
        //dossier date de modification du dossier
        String dateModificationDossier = dossierConvertDTO.getDateModificationDossier().atOffset(ZoneOffset.UTC).toLocalDate().format(formatter);
        dossierThymDTO.setDateModificationDossier(dateModificationDossier);
        dossierThymDTO.setDossierStatus(dossierConvertDTO.getDossierStatus());
        dossierThymDTO.setClient(clientThymDTO);
        dossierThymDTO.setVehicule(vehiculeThymDTO);
        return dossierThymDTO;
    }
    
    /**
     * Empêche la création d'un vehicule si l'immatriculation existe déjà. Vérifie si un vehicule existe en fonction de son immatriculation.
     *
     * @param immatriculation immatriculation du vehicule
     * @return Boolean
     */
    Mono<Boolean> immatriculationExiste(String immatriculation) {
        return webClient.get()
                        .uri("/queries/vehiculeExists/" + immatriculation)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(Boolean.class);
        //                        .block();
        //        return restClient.get()
        //                         .uri("/queries/vehiculeExists/" + immatriculation)
        //                         .accept(MediaType.APPLICATION_JSON)
        //                         .retrieve()
        //                         .body(Boolean.class);
    }
    
    
    /**
     * Affiche la page de succès
     *
     * @return la vue success
     */
    @GetMapping("/success")
    public String showSuccess() {
        return "success";
    }
}
