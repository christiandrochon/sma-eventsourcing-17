package fr.cdrochon.thymeleaffrontend.controller.client;

import fr.cdrochon.thymeleaffrontend.dtos.client.AdresseClientDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.ClientPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.ClientStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.PaysDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeStatusDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class CreateClientController {
    @Autowired
    private WebClient webClient;
    final RestClient restClient = RestClient.create("http://localhost:8092");
    
    @GetMapping("/createClient")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarage(Model model) {
//        ClientPostDTO clientDTO = new ClientPostDTO();
//        clientDTO.setAdresse(new AdresseClientDTO());
        
        if(!model.containsAttribute("clientDTO")) {
            model.addAttribute("clientDTO", new ClientPostDTO());
        }
        model.addAttribute("clientStatuses", List.of(ClientStatusDTO.values()));
        model.addAttribute("paysList", List.of(PaysDTO.values()));
        model.addAttribute("paysDefaut", PaysDTO.FRANCE);
        return "client/createClientForm";
    }
    
    @PostMapping(value = "/createClient")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarage(@Valid @ModelAttribute("clientDTO") ClientPostDTO clientDTO, BindingResult result, RedirectAttributes redirectAttributes,
                               Model model) {
        if(result.hasErrors()) {
            model.addAttribute("clientDTO", clientDTO);
            model.addAttribute("clientStatuses", List.of(ClientStatusDTO.values()));
            model.addAttribute("paysList", List.of(PaysDTO.values()));
            return "client/createClientForm";
        }
        try {
            webClient.post()
                            .uri("/commands/createClient")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(clientDTO)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
            
//            clientDTO.setAdresse(clientDTO.getAdresse());
            
            //                        Client client = new Client();
            //                        //            garage.setId(garageDTO.getId());
            //                        client.setNomClient(clientDTO.getNomClient());
            //
            //                        client.setPrenomClient(clientDTO.getPrenomClient());
            //                        client.setMailClient(clientDTO.getMailClient());
            //                        client.setTelClient(clientDTO.getTelClient());
            //                        //FIXME : utiliser l'adresse DTO ?
            //                        client.setAdresse(clientDTO.getAdresse());
            //                        client.setClientStatus(clientDTO.getClientStatus());
            
            
//            restClient.post().uri("/commands/createClient")
//                      //                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
//                      //                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                      .contentType(MediaType.APPLICATION_JSON)
//                      .body(clientDTO).retrieve().toBodilessEntity();
            
//            WebClient webClient = WebClient.builder()
//                                           .baseUrl("http://localhost:8092")
//                                           //                                           .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType
//                                           //                                           .APPLICATION_JSON_VALUE)
//                                           .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                                           .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
//                                           .build();
//
//            webClient.post()
//                     .uri("/commands/createClient")
//                     .contentType(MediaType.APPLICATION_JSON)
//                     .acceptCharset(StandardCharsets.UTF_8)
//                     .bodyValue(clientDTO)
//                     .retrieve()
//                     .bodyToMono(String.class)
//                     .subscribe(response -> {
//                         System.out.println("Response: " + response);
//                     }, error -> {
//                         System.err.println("Error: " + error.getMessage());
//                     });
            //                                       .block();
            
            System.out.println("Dossier created successfully");
            
            //rafraichissement
            redirectAttributes.addFlashAttribute("successMessage", "Client created successfully");
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
