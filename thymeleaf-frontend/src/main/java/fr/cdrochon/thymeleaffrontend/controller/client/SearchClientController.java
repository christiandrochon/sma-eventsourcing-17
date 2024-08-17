package fr.cdrochon.thymeleaffrontend.controller.client;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientPostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Controller
public class SearchClientController {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    
    @Autowired
    private WebClient webClient;
    private final RestClient restClient;
    
    public SearchClientController(RestClient restClient) {
        this.restClient = restClient;
    }
    
    /**
     * Affiche l'ensemble des clients dans la liste déroulante du formulaire de recherche de client
     * @param model model de la vue
     * @return la vue searchClientForm
     */
    @GetMapping(value = "/searchclient")
    public String searchClient(Model model) {
        List<ClientPostDTO> clients =
                restClient.get()
                          .uri(externalServiceUrl + "/queries/clients")
                          //                          .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                          .retrieve().body(new ParameterizedTypeReference<>() {
                          });
        model.addAttribute("clients", clients);
        model.addAttribute("clientPostDTO", new ClientPostDTO());
        
        return "client/searchClientForm";
    }
    
    /**
     * Recherche un client par son id
     * @param id   id du client
     * @param model model de la vue
     * @return la vue client/view
     */
    @GetMapping("/searchclient/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public String searchClientById(@PathVariable String id, Model model) {
        ClientPostDTO client = restClient.get().uri(externalServiceUrl + "/queries/clients/" + id)
                                         //                                  .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " +
                                         //                                  getJwtTokenValue()))
                                         .retrieve().body(new ParameterizedTypeReference<>() {
                });
        model.addAttribute("client", client);
        return "client/view";
    }

}
