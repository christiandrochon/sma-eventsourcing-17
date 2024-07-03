package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculePostDTO;
import fr.cdrochon.thymeleaffrontend.entity.vehicule.Vehicule;
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
public class CreateVehiculeController {
    
    RestClient restClient = RestClient.create("http://localhost:8092");
    
    @GetMapping("/createVehicule")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createVehicule(Model model) {
        if (!model.containsAttribute("vehiculeDTO")) {
            model.addAttribute("vehiculeDTO", new VehiculePostDTO());
        }
        return "vehicule/createVehiculeForm";
    }
    
    @PostMapping(value = "/createVehicule")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarage(@ModelAttribute VehiculePostDTO vehiculePostDTO, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if(result.hasErrors()) {
            model.addAttribute("vehiculeDTO", vehiculePostDTO);
            //            return new ModelAndView("createGarageForm");
            return "/vehicule/createVehiculeForm";
        }
        try {
            Vehicule vehicule = new Vehicule();
            //            garage.setId(garageDTO.getId());
            vehicule.setImmatriculationVehicule(vehiculePostDTO.getImmatriculationVehicule());
            
            vehicule.setDateMiseEnCirculationVehicule(vehiculePostDTO.getDateMiseEnCirculationVehicule());
            
            //            MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(garage);
            //            mappingJacksonValue.setSerializationView(gara);
            
            //           ResponseEntity<Void> responseEntity =
            restClient.post().uri("/commands/createVehicule")
                      //                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                      //                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(vehicule).retrieve().toBodilessEntity();
            
            //            System.out.println(responseEntity);
            //            return new ModelAndView("redirect:/garages");
            return "redirect:/vehicules";
        } catch(Exception e) {
            System.out.println("VEHICULE THYMELEAF ERRRRRRRRRRRRRRRRROOR : " + e.getMessage());
            //            return new ModelAndView("createGarageForm");
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("clientDTO", vehiculePostDTO); // Re-add garageDTO to the model if there's an error
            //            return new ModelAndView("redirect:/createGarage");
            return "redirect:/createVehicule";
        }
    }
}
