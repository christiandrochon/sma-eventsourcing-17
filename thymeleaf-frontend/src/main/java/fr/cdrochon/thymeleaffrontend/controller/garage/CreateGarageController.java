package fr.cdrochon.thymeleaffrontend.controller.garage;

import fr.cdrochon.thymeleaffrontend.dtos.garage.GarageAdresseDTO;
import fr.cdrochon.thymeleaffrontend.dtos.garage.GaragePostDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
public class CreateGarageController {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    private final RestClient restClient;
    
    public CreateGarageController(RestClient restClient) {
        this.restClient = restClient;
    }
    
    @GetMapping("/createGarage")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarage(Model model) {
        GaragePostDTO garageDTO = new GaragePostDTO();
        garageDTO.setAdresse(new GarageAdresseDTO());
        
        //rafraichissement de l'ecran
        if(!model.containsAttribute("garageDTO")) {
            model.addAttribute("garageDTO", new GaragePostDTO());
        }
        return "garage/createGarageForm";
    }
    
    
    @PostMapping("/createGarage")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarage(@Valid @ModelAttribute("garageDTO") GaragePostDTO garageDTO, BindingResult result, RedirectAttributes redirectAttributes,
                               Model model) {
        if(result.hasErrors()) {
            model.addAttribute("garageDTO", garageDTO);
            result.getAllErrors().forEach(err -> log.error("LOG ERROR : {}", err.getDefaultMessage()));
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + result.getAllErrors().get(0).getDefaultMessage());
            return "garage/createGarageForm";
        }
        try {
            
            garageDTO.setAdresse(garageDTO.getAdresse());
            restClient.post().uri(externalServiceUrl + "/commands/createGarage")
                      //                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                      //                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(garageDTO).retrieve().toBodilessEntity();
            
            //TODO : liste des garages non mis à jour après création d'un garage
            //rafraichissement
            redirectAttributes.addFlashAttribute("successMessage", "Garage created successfully");
            return "redirect:/garages";
        } catch(Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("garageDTO", garageDTO);
            return "redirect:/createGarage";
        }
    }
    
}
