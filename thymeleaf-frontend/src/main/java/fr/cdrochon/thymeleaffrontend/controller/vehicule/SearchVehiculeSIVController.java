package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.siv.GetImmatriculationDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.siv.VehiculeSIVConvertDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.siv.VehiculeSIVDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;

import static fr.cdrochon.thymeleaffrontend.dtos.vehicule.siv.VehiculeDTOMapper.toVehiculeSIVConvertDTO;

@Controller
public class SearchVehiculeSIVController {
    
    @Value("${vehicule.json.path}") // URL du fichier JSON configuré dans application.properties
    private String vehiculeJsonPath;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    
    public SearchVehiculeSIVController(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Affiche le formulaire de recherche d'un véhicule par son numéro d'immatriculation
     *
     * @param model Model pour les données à afficher
     * @return la vue searchVehiculeSIVForm
     */
    @GetMapping("/searchvehiculesiv")
    public String searchVehiculeSIVForm(Model model) {
        if(!model.containsAttribute("getImmatDTO")) {
            model.addAttribute("getImmatDTO", new GetImmatriculationDTO());
        }
        return "vehicule/searchVehiculeSIVForm";
    }
    
    /**
     * Recherche d'un véhicule par son numéro d'immatriculation
     *
     * @param getImmatriculationDTO DTO correspondant à l'entité d'un véhicule SIV. Ce DTO converti les données recues de VehiculeDTO en données
     * @param result                BindingResult pour la validation des données
     * @param redirectAttributes    RedirectAttributes pour les attributs de redirection
     * @param model                 Model pour les données à afficher
     * @return la vue resultatSIVForm
     */
    @PostMapping("/searchvehiculesiv")
    public String searchVehicule(@Valid @ModelAttribute("getImmatDTO") GetImmatriculationDTO getImmatriculationDTO, BindingResult result,
                                 RedirectAttributes redirectAttributes, Model model) {
        if(result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));
            model.addAttribute("getImmatDTO", getImmatriculationDTO);
            return "vehicule/searchVehiculeSIVForm";
        }
        
        String immatriculation = getImmatriculationDTO.getImmatriculation();
        Resource resource = resourceLoader.getResource(vehiculeJsonPath);
        try(InputStream inputStream = resource.getInputStream()) {
            VehiculeSIVDTO[] vehicules = objectMapper.readValue(inputStream, VehiculeSIVDTO[].class);
            for(VehiculeSIVDTO vehicule : vehicules) {
                if(vehicule.getImmatriculation().equalsIgnoreCase(immatriculation)) {
                    
                    VehiculeSIVConvertDTO vehiculeConverted = toVehiculeSIVConvertDTO(vehicule);
                    model.addAttribute("vehiculeConverted", vehiculeConverted);
                    return "vehicule/resultatSIVForm"; // Page affichant les détails du véhicule
                }
            }
            
            redirectAttributes.addFlashAttribute("errorMessage",
                                                 "Erreur de recherche. Le numéro d'immatriculation '" + immatriculation + "' n'a pas été trouvé.");
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
            redirectAttributes.addFlashAttribute("urlRedirection", "/searchvehiculesiv");
            redirectAttributes.addFlashAttribute("vehiculeConverted", vehicules); // Re-add garageDTO to the model if there's an error
            return "redirect:/error";
        } catch(IOException e) {
            model.addAttribute("error", "Erreur lors de la lecture du fichier JSON: " + e.getMessage());
            return "vehicule/searchVehiculeSIVForm";
        }
    }
}
