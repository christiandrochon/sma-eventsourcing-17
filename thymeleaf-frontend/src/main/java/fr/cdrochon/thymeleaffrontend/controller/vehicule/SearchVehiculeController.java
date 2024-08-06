package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner.GetImmatriculationDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner.VehiculeDTO;
import fr.cdrochon.thymeleaffrontend.exception.InvalidDateFormatException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
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
     * Recherche asynchrone d'un véhicule dans la bdd grâce à l'utilisation de Mono de Project Reactor et WebClient de Spring WebFlux
     *
     * @param immatriculation numéro d'immatriculation du véhicule
     * @param model           Model pour les données à afficher
     * @return la vue resultSearchVehiculeView
     */
    @GetMapping("/searchvehicule")
    public Mono<String> searchVehiculeAsync(@RequestParam("immatriculation") String immatriculation, Model model) {
        Mono<VehiculeDTO> vehiculeMono = webClient.get()
                                                  .uri("/queries/vehicules/immatriculation/" + immatriculation)
                                                  .accept(MediaType.APPLICATION_JSON)
                                                  .header("Content-Type", "application/json")
                                                  .retrieve()
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
                        model.addAttribute("message",
                                           "Erreur de conversion de date : " + vehicule.getDateMiseEnCirculationVehicule() + ". Le format attendu est de type" +
                                                   " dd MMM yyy");
                        model.addAttribute("alertClass", "alert-danger");
                        model.addAttribute("redirectUrl", "/searchVehicule");
                        return Mono.just("redirect:/error");
                    }
                    
                    // Ajout des attributs au modèle en cas de succès
                    model.addAttribute("vehicule", vehicule);
                    return Mono.just("redirect:/vehicule/inner/resultSearchVehiculeView");
                })
                .switchIfEmpty(Mono.defer(() -> {
                    model.addAttribute("errorMessage", "Véhicule non trouvé pour l'immatriculation " + immatriculation);
                    model.addAttribute("alertClass", "alert-danger");
                    model.addAttribute("urlRedirection", "/searchvehicule");
                    return Mono.just("error");
                }))
                .onErrorResume(e -> {
                    model.addAttribute("errorMessage", "Erreur lors de la recherche pour l'immatriculation " + immatriculation);
                    model.addAttribute("alertClass", "alert-danger");
                    model.addAttribute("urlRedirection", "/showvehicule");
                    return Mono.just("error");
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
            Mono<VehiculeDTO> vehiculeMono = webClient.get()
                                                      .uri("/queries/vehicules/immatriculation/" + getImmatDTO.getImmatriculation())
                                                      .accept(MediaType.APPLICATION_JSON)
                                                      .header("Content-Type", "application/json")
                                                      .retrieve()
                                                      .bodyToMono(VehiculeDTO.class);
            
            VehiculeDTO vehicule = vehiculeMono.block();
            
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy");
            try {
                Date date = inputFormat.parse(vehicule.getDateMiseEnCirculationVehicule());
                String dateMiseEnCirculationVehicule = outputFormat.format(date);
                vehicule.setDateMiseEnCirculationVehicule(dateMiseEnCirculationVehicule);
            } catch(DateTimeParseException e) {
                throw new InvalidDateFormatException("Erreur de conversion de date : " + vehicule.getDateMiseEnCirculationVehicule() + ". Le format attendu" +
                                                             "est de type dd MMM yyy");
            }
            
            model.addAttribute("vehicule", vehicule);
            log.info("Vehicule recherché : {}", getImmatDTO.getImmatriculation());
            return "/vehicule/inner/resultSearchVehiculeView";
            
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
    
    
    /**
     * Affiche un ecran d'erreur dans le cas où le véhicule recherché ne serait pas trouvé dans la bdd
     *
     * @param message     message d'erreur
     * @param alertClass  classe de l'alerte
     * @param redirectUrl url de redirection
     * @param model       Model pour les données à afficher
     * @return la vue error
     */
    @GetMapping("/error")
    public String handleError(@RequestParam("message") String message,
                              @RequestParam("alertClass") String alertClass,
                              @RequestParam("redirectUrl") String redirectUrl,
                              Model model) {
        model.addAttribute("message", message);
        model.addAttribute("alertClass", alertClass);
        model.addAttribute("redirectUrl", redirectUrl);
        return "error";  // Retourne le nom de la vue (fichier HTML) à afficher
    }
}
