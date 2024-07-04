package fr.cdrochon.thymeleaffrontend.controller.client;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientPostDTO;
import fr.cdrochon.thymeleaffrontend.entity.client.Client;
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
public class CreateClientController {
    
    RestClient restClient = RestClient.create("http://localhost:8092");
    
    @GetMapping("/createClient")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarage(Model model) {
        if (!model.containsAttribute("clientDTO")) {
            model.addAttribute("clientDTO", new ClientPostDTO());
        }
        return "createClientForm";
    }
    
    @PostMapping(value = "/createClient")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarage(@Valid @ModelAttribute ClientPostDTO clientDTO, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if(result.hasErrors()) {
            model.addAttribute("clientDTO", clientDTO);
            //            return new ModelAndView("createGarageForm");
            return "createClientForm";
        }
        try {
            Client client = new Client();
            //            garage.setId(garageDTO.getId());
            client.setNomClient(clientDTO.getNomClient());
            
            client.setPrenomClient(clientDTO.getPrenomClient());
            client.setMailClient(clientDTO.getMailClient());
            client.setTelClient(clientDTO.getTelClient());
            //FIXME : utiliser l'adresse DTO ?
            client.setAdresse(clientDTO.getAdresse());
            
            //            MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(garage);
            //            mappingJacksonValue.setSerializationView(gara);
            
            //           ResponseEntity<Void> responseEntity =
            restClient.post().uri("/commands/createClient")
                      //                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                      //                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(client).retrieve().toBodilessEntity();
            
            //            System.out.println(responseEntity);
            //            return new ModelAndView("redirect:/garages");
            return "redirect:/clients";
        } catch(Exception e) {
            System.out.println("ERRRRRRRRRRRRRRRRROOR : " + e.getMessage());
            //            return new ModelAndView("createGarageForm");
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("clientDTO", clientDTO); // Re-add garageDTO to the model if there's an error
            //            return new ModelAndView("redirect:/createGarage");
            return "redirect:/createClient";
        }
    }
}
