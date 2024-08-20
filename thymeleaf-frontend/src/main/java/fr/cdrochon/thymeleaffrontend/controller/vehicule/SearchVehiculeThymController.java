package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner.GetImmatriculationDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner.VehiculeDTO;
import fr.cdrochon.thymeleaffrontend.exception.InvalidDateFormatException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.Date;


@Controller
@Slf4j
public class SearchVehiculeThymController {

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
     * Recherche d'un véhicule dans la bdd de manière asynchrone, en utilisant son numéro d'immatriculation sur le service queries/vehicules/immatriculation
     *
     * @param getImmatDTO DTO correspondant à l'entité d'un véhicule.
     * @param result      BindingResult pour la validation des données
     * @param model       Model pour les données à afficher
     * @return la vue searchVehiculeForm
     */
    @GetMapping("/searchvehicule")
    public Mono<String> searchVehiculeAsync(@Valid @ModelAttribute("getImmatDTO") GetImmatriculationDTO getImmatDTO, BindingResult result,
                                            Model model) {
        if(result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));
            model.addAttribute("getImmatDTO", getImmatDTO);
            return Mono.just("vehicule/inner/searchVehiculeForm");
        }
        
        Mono<VehiculeDTO> vehiculeMono =
                webClient.get()
                         .uri("/queries/vehicules/immatriculation/" + getImmatDTO.getImmatriculation())
                         .accept(MediaType.APPLICATION_JSON)
                         .header("Content-Type", "application/json")
                         .retrieve()
                         .onStatus(HttpStatus.NOT_FOUND::equals,
                                   clientResponse -> Mono.empty())
                         .onStatus(HttpStatusCode::is5xxServerError,
                                   clientResponse -> Mono.error(new WebClientResponseException("Erreur interne du serveur",
                                                                                               500,
                                                                                               "Erreur de recherche du véhicule",
                                                                                               null,
                                                                                               null,
                                                                                               null)))
                         .bodyToMono(VehiculeDTO.class);
        
        return vehiculeMono
                .flatMap(vehicule -> {
                    
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy");
                    try {
                        Date date = inputFormat.parse(vehicule.getDateMiseEnCirculationVehicule());
                        String dateMiseEnCirculationVehicule = outputFormat.format(date);
                        vehicule.setDateMiseEnCirculationVehicule(dateMiseEnCirculationVehicule);
                    } catch(ParseException e) {
                        return Mono.error(new InvalidDateFormatException("Erreur de conversion de date. Le format attendu est de type 'dd MMMM yyyy'"));
                    }
                    
                    // Ajout des attributs au modèle en cas de succès
                    model.addAttribute("vehicule", vehicule);
                    return Mono.just("vehicule/inner/resultSearchVehiculeView");
                })
                .switchIfEmpty(Mono.defer(() -> {
                    model.addAttribute("errorMessage", "Véhicule non trouvé pour l'immatriculation '" + getImmatDTO.getImmatriculation() + "'");
                    model.addAttribute("alertClass", "alert-danger");
                    model.addAttribute("urlRedirection", "/showvehicule");
                    return Mono.just("redirect:/error");
                }))
                
                .onErrorResume(e -> {
                    model.addAttribute("errorMessage", e.getMessage());
                    model.addAttribute("alertClass", "alert-danger");
                    model.addAttribute("urlRedirection", "/showvehicule");
                    return Mono.just("redirect:/error");
                });
    }
    
    
    /**
     * Recherche d'un véhicule dans la bdd de manière synchrone, en utilisant son numéro d'immatriculation sur le service queries/vehicules/immatriculation
     *
     * @param getImmatDTO        DTO correspondant à l'entité d'un véhicule.
     * @param result             BindingResult pour la validation des données
     * @param redirectAttributes RedirectAttributes pour les attributs de redirection
     * @param model              Model pour les données à afficher
     * @return la vue searchVehiculeForm
     */
    //        @GetMapping("/searchvehicule")
    public String searchVehiculeSync(@Valid @ModelAttribute("getImmatDTO") GetImmatriculationDTO getImmatDTO, BindingResult result,
                                     RedirectAttributes redirectAttributes, Model model) {
        
        if(result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));
            model.addAttribute("getImmatDTO", getImmatDTO);
            return "vehicule/inner/searchVehiculeForm";
        }
        
        try {
            //appel du ms dossier pour la recherche d'un vehicule
            Mono<VehiculeDTO> vehiculeMono =
                    webClient.get()
                             .uri("/queries/vehicules/immatriculation/" + getImmatDTO.getImmatriculation())
                             .accept(MediaType.APPLICATION_JSON)
                             .header("Content-Type", "application/json")
                             .retrieve()
                             .bodyToMono(VehiculeDTO.class);
            
            VehiculeDTO vehicule = vehiculeMono.block();
            
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy");
            try {
                assert vehicule != null;
                Date date = inputFormat.parse(vehicule.getDateMiseEnCirculationVehicule());
                String dateMiseEnCirculationVehicule = outputFormat.format(date);
                vehicule.setDateMiseEnCirculationVehicule(dateMiseEnCirculationVehicule);
            } catch(DateTimeParseException e) {
                throw new InvalidDateFormatException("Erreur de conversion de date : " + vehicule.getDateMiseEnCirculationVehicule() + ". Le format attendu" +
                                                             "est de type dd MMM yyy");
            }
            
            model.addAttribute("vehicule", vehicule);
            return "vehicule/inner/resultSearchVehiculeView";
            
            //TODO  si le nombre de vehciules renovyés sont multiples (plusieurs vehicules avec la même immatriculation = grosse erreur!!)
        } catch(WebClientResponseException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                                                 "Erreur de recherche. Le numéro d'immatriculation '" + getImmatDTO.getImmatriculation() + "' n'a pas été " +
                                                         "trouvé.");
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
            redirectAttributes.addFlashAttribute("urlRedirection", "/searchvehicule");
            return "redirect:/error";
        } catch(Exception e) {
            log.error("Erreur lors de la recherche du véhicule : {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                                                 "Erreur de recherche d'un véhicule. Merci de communiquer le contenu de l'erreur suivante au développeur :'" + e.getMessage() + "' ");
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
            redirectAttributes.addFlashAttribute("urlRedirection", "/searchvehicule");
            redirectAttributes.addFlashAttribute("getImmatDTO", getImmatDTO); // Re-add garageDTO to the model if there's an error
            return "redirect:/error";
        }
    }
}
