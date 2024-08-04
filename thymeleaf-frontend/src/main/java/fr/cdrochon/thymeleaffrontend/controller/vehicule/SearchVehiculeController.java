package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner.GetImmatriculationDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner.VehiculeDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
                                                     .bodyToMono(VehiculeDTO.class);//.block();
            VehiculeDTO vehicule = vehiculeDTO.block();
            
            // Conversion des formats de date et envoi vers la vue
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
            
        } catch(WebClientResponseException e) {
            log.error("Erreur lors de la recherche du véhicule : {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            redirectAttributes.addFlashAttribute("errorMessage",
                                                 "Erreur de recherche d'un véhicule. Merci de communiquer le contenu de l'erreur suivante au développeur : '" + e.getResponseBodyAsString() + "'");
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
            redirectAttributes.addFlashAttribute("urlRedirection", "/searchvehicule");
            redirectAttributes.addFlashAttribute("getImmatDTO", getImmatDTO);
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
}
