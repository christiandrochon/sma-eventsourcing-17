package fr.cdrochon.thymeleaffrontend.controller.client;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.ClientStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.PaysDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
public class CreateClientController {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    private final WebClient webClient;
    private final RestClient restClient;
    
    public CreateClientController(WebClient webClient, RestClient restClient) {
        this.webClient = webClient;
        this.restClient = restClient;
    }
    
    /**
     * Affiche le formulaire de création d'un client
     *
     * @param model modèle du client: permet de passer des attributs à la vue
     * @return la vue createClientForm
     */
    @GetMapping("/createClient")
    public String createGarage(Model model) {
        if(!model.containsAttribute("clientDTO")) {
            model.addAttribute("clientDTO", new ClientPostDTO());
        }
        model.addAttribute("clientStatuses", List.of(ClientStatusDTO.values()));
        model.addAttribute("paysList", List.of(PaysDTO.values()));
        model.addAttribute("valeurPaysParDefaut", PaysDTO.valeurPaysParDefaut());
        return "client/createClientForm";
    }
    
    /**
     * Création d'un client via un formulaire.
     *
     * @param clientDTO          le client à créer
     * @param result             le résultat de la validation du formulaire
     * @param redirectAttributes attributs de redirection
     * @param model              modèle du client: permet de passer des attributs à la vue
     * @return la vue clients si la création a réussi, sinon la vue createClientForm
     */
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
                     .doOnNext(response -> log.info("Client created successfully : " + response))
                     .doOnError(error -> log.error("An error occurred: {}", error.getMessage()))
                     .block();
            
            //rafraichissement
            redirectAttributes.addFlashAttribute("successMessage", "Client created successfully");
            return "redirect:/clients";
        } catch(Exception e) {
            
            log.error("An error occurred: {}", e.getMessage());
            //            return new ModelAndView("createGarageForm");
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("clientDTO", clientDTO); // Re-add garageDTO to the model if there's an error
            return "redirect:/createClient";
        }
    }
}
