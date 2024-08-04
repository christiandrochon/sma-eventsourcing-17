package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner.GetImmatriculationDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner.VehiculeDTO;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@Slf4j
public class SearchVehiculeController {
    @Autowired
    private WebClient webClient;
    
    /**
     * Affiche le formulaire de recherche en interne d'un véhicule par son numéro d'immatriculation
     *
     * @param model Model pour les données à afficher
     * @return la vue searchVehiculeForm
     */
    @GetMapping("/showvehicule")
    public String searchVehiculeForm(Model model) {
        if(!model.containsAttribute("getImmatDTO")) {
            model.addAttribute("getImmatDTO", new GetImmatriculationDTO());
        }
        return "vehicule/inner/searchVehiculeForm";
    }
    
    /**
     * Envoi de la requête de recherche d'un véhicule par son numéro d'immatriculation sur le service queries/vehicules/immatriculation
     *
     * @param getImmatDTO        DTO correspondant à l'entité d'un véhicule.
     * @param result             BindingResult pour la validation des données
     * @param redirectAttributes RedirectAttributes pour les attributs de redirection
     * @param model              Model pour les données à afficher
     * @return la vue searchVehiculeForm
     */
    @GetMapping("/searchvehicule")
    public String searchVehicule(@Valid @ModelAttribute("getImmatDTO") GetImmatriculationDTO getImmatDTO, BindingResult result,
                                 RedirectAttributes redirectAttributes, Model model) {
        
        if(result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));
            model.addAttribute("getImmatDTO", getImmatDTO);
            return "vehicule/inner/searchVehiculeForm";
        }
        
        try {
            //appel du ms dossier pour la recherche d'un vehicule
            Mono<VehiculeDTO> vehiculeDTO = webClient.get()
                                                     .uri("/queries/vehicules/immatriculation/" + getImmatDTO.getImmatriculation())
                                                     .accept(MediaType.APPLICATION_JSON)
                                                     .header("Content-Type", "application/json")
                                                     .retrieve()
                                                     .bodyToMono(VehiculeDTO.class);
            
            VehiculeDTO vehicule = vehiculeDTO.block();
            
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy");
            try {
                Date date = inputFormat.parse(vehicule.getDateMiseEnCirculationVehicule());
                String dateMiseEnCirculationVehicule = outputFormat.format(date);
                vehicule.setDateMiseEnCirculationVehicule(dateMiseEnCirculationVehicule);
            } catch(Exception e) {
                e.printStackTrace();
            }
            
            model.addAttribute("vehicule", vehicule);
            log.info("Vehicule recherché : {}", getImmatDTO.getImmatriculation());
            return "/vehicule/inner/resultSearchVehiculeView";
            
            //            vehiculeDTO
            //                    .doOnNext(vehicule -> {
            //                        // Conversion des formats de date et envoi vers la vue
            //                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            //                        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy");
            //                        try {
            //                            Date date = inputFormat.parse(vehicule.getDateMiseEnCirculationVehicule());
            //                            String dateMiseEnCirculationVehicule = outputFormat.format(date);
            //                            vehicule.setDateMiseEnCirculationVehicule(dateMiseEnCirculationVehicule);
            //
            //                        } catch(Exception e) { //TODO gerer les exceptions de date
            //                            e.printStackTrace();
            //                        }
            //
            //                        model.addAttribute("vehicule", vehicule);
            //                        log.info("Vehicule recherché : {}", getImmatDTO.getImmatriculation());
            //                    })
            //                    .onErrorResume(e -> {
            //                        redirectAttributes.addFlashAttribute("errorMessage",
            //                                                             "Erreur de recherche d'un véhicule. Merci de communiquer le contenu de l'erreur
            //                                                             suivante au " +
            //                                                                     "développeur : '" + e.getMessage() + "'");
            //                        redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
            //                        redirectAttributes.addFlashAttribute("urlRedirection", "/searchvehicule");
            //                        redirectAttributes.addFlashAttribute("getImmatDTO", getImmatDTO); // Re-add garageDTO to the model if there's an error
            //                        return Mono.empty();
            //                    })
            //                    .block();
            //
            //
            //            if(vehiculeDTO != null) {
            //                return "/vehicule/inner/resultSearchVehiculeView";
            //            }
            //            else {
            //                return "redirect:/error";
            //            }
            
        } catch(WebClientResponseException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                                                 "Erreur de recherche. Le numéro d'immatriculation '" + getImmatDTO.getImmatriculation() + "' n'a pas été trouvé.");
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
            redirectAttributes.addFlashAttribute("urlRedirection", "/searchvehicule");
            return "redirect:/error";
        } catch(Exception e) {
            log.error("Erreur lors de la recherche du véhicule : {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                                                 "Erreur de recherche d'un véhicule. Merci de communiquer le contenu de l'erreur suivante au développeur : '" + e.getMessage() + "'");
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
            redirectAttributes.addFlashAttribute("urlRedirection", "/searchvehicule");
            redirectAttributes.addFlashAttribute("getImmatDTO", getImmatDTO); // Re-add garageDTO to the model if there's an error
            return "redirect:/error";
            //            return "vehicule/inner/searchVehiculeForm";
            
        }
    }
    
    private Mono<Void> handleData(String data, ServerWebExchange exchange) {
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return Mono.empty();
    }
    
    private Mono<Void> redirectToUrl(String url, String paramName, String paramValue) {
        URI uri = UriComponentsBuilder.fromUriString(url)
                                      .queryParam(paramName, paramValue)
                                      .build()
                                      .toUri();
        return Mono.empty();
    }
    
    private Mono<Void> redirectTo(RedirectAttributes redirectAttributes, GetImmatriculationDTO getImmatDTO) {
        redirectAttributes.addFlashAttribute("errorMessage",
                                             "Erreur de recherche d'un véhicule. Merci de " +
                                                     "communiquer le contenu de l'erreur suivante au développeur : '");
        redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        redirectAttributes.addFlashAttribute("urlRedirection", "/searchvehicule");
        redirectAttributes.addFlashAttribute("getImmatDTO", getImmatDTO);
        //        return Mono.just("redirect:/error");
        return Mono.empty();
    }
}
