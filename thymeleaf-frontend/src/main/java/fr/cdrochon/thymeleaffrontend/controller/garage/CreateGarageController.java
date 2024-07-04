package fr.cdrochon.thymeleaffrontend.controller.garage;

import fr.cdrochon.thymeleaffrontend.dtos.garage.GaragePostDTO;
import fr.cdrochon.thymeleaffrontend.entity.garage.Garage;
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
    
    RestClient restClient = RestClient.create("http://localhost:8092");
    
    @GetMapping("/createGarage")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarage(Model model) {
        if (!model.containsAttribute("garageDTO")) {
            model.addAttribute("garageDTO", new GaragePostDTO());
        }
        return "createGarageForm";
    }
    
    
//    @PostMapping("/createGarage")
//    public ModelAndView createGarage(@Valid @ModelAttribute("garageDTO") GaragePostDTO garageDTO, BindingResult result, RedirectAttributes redirectAttributes) {
//        if (result.hasErrors()) {
//            return new ModelAndView("createGarageForm");
//        }
//        try {
//            Garage garage = new Garage();
//            garage.setNomGarage(garageDTO.getNomGarage());
//            garage.setEmailContactGarage(garageDTO.getMailResp());
//            garage.setAdresseGarage(garageDTO.getAdresse());
//
//            restClient.post().uri("/commands/creategarage")
//                      .contentType(MediaType.APPLICATION_JSON)
//                      .body(garage).retrieve().toBodilessEntity();
//
//            return new ModelAndView("redirect:/garages");
//        } catch(Exception e) {
//            System.out.println("ERROR : " + e.getMessage());
//            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
//            redirectAttributes.addFlashAttribute("garageDTO", garageDTO); // Re-add garageDTO to the model if there's an error
//            return new ModelAndView("redirect:/createGarage");
//        }
//    }
    
    @PostMapping("/createGarage")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarage(@Valid @ModelAttribute("garageDTO") GaragePostDTO garageDTO, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if(result.hasErrors()) {
            model.addAttribute("garageDTO", garageDTO);
//            return new ModelAndView("createGarageForm");
            return "createGarageForm";
        }
        try {
            Garage garage = new Garage();
            //            garage.setId(garageDTO.getId());
            garage.setNomGarage(garageDTO.getNomGarage());
            garage.setEmailContactGarage(garageDTO.getMailResp());
            garage.setAdresseGarage(garageDTO.getAdresse());

            //            MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(garage);
            //            mappingJacksonValue.setSerializationView(gara);

            //           ResponseEntity<Void> responseEntity =
            restClient.post().uri("/commands/createGarage")
                      //                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                      //                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(garage).retrieve().toBodilessEntity();

            //            System.out.println(responseEntity);
//            return new ModelAndView("redirect:/garages");
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
