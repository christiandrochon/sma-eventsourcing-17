package fr.cdrochon.thymeleaffrontend.controller.garage;

import fr.cdrochon.thymeleaffrontend.dtos.garage.GarageAdresseDTO;
import fr.cdrochon.thymeleaffrontend.dtos.garage.GaragePostDTO;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CreateGarageController {
    
    final RestClient restClient = RestClient.create("http://localhost:8092");

    @GetMapping("/createGarage")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarage(Model model) {
        GaragePostDTO garageDTO = new GaragePostDTO();
        garageDTO.setAdresse(new GarageAdresseDTO());
        
        if (!model.containsAttribute("garageDTO")) {
            model.addAttribute("garageDTO", new GaragePostDTO());
        }
        return "garage/createGarageForm";
    }
    
    
    @PostMapping("/createGarage")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarage(@Valid @ModelAttribute("garageDTO") GaragePostDTO garageDTO, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if(result.hasErrors()) {
            model.addAttribute("garageDTO", garageDTO);
//            return new ModelAndView("createGarageForm");
            return "garage/createGarageForm";
        }
        try {
            
            garageDTO.setAdresse(garageDTO.getAdresse());
            restClient.post().uri("/commands/createGarage")
                      //                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                      //                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(garageDTO).retrieve().toBodilessEntity();
            
            //rafraichissement
            redirectAttributes.addFlashAttribute("successMessage", "Garage created successfully");
            return "redirect:/garages";
        } catch(Exception e) {
            System.out.println("ERRRRRRRRRRRRRRRRROOR : " + e.getMessage());
//            return new ModelAndView("createGarageForm");
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("garageDTO", garageDTO); // Re-add garageDTO to the model if there's an error
//            return new ModelAndView("redirect:/createGarage");
            return "redirect:/createGarage";
        }
    }
    
}
