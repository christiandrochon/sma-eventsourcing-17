package fr.cdrochon.thymeleaffrontend.controller.garage;

import fr.cdrochon.thymeleaffrontend.dtos.garage.GarageGetDTO;
import fr.cdrochon.thymeleaffrontend.dtos.garage.GaragePostDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;

import java.util.List;

@Controller
public class GarageController {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    private final RestClient restClient;
    
    public GarageController(RestClient restClient) {
        this.restClient = restClient;
    }
    
    /**
     * Affiche les données d'un garage
     *
     * @param id    id du garage
     * @param model model
     * @return la vue garage/view
     */
    @GetMapping(value = "/garage/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public String garageById(@PathVariable String id, Model model) {
        GaragePostDTO garage = restClient.get().uri(externalServiceUrl + "/queries/garages/" + id)
                                         //                                  .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " +
                                         //                                  getJwtTokenValue()))
                                         .retrieve().body(new ParameterizedTypeReference<>() {
                });
        model.addAttribute("garage", garage);
        return "garage/view";
    }
    
    /**
     * Requete vers le ms garage avec RestClient
     *
     * @param model model
     * @return la vue garages.html
     */
    @GetMapping("/garages")
    //    @PreAuthorize("hasAuthority('USER')")
    public String garages(Model model) {
        List<GarageGetDTO> garages = restClient.get().uri(externalServiceUrl+ "/queries/garages")
                                               //                                         .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION,
                                               //                                         "Bearer " + getJwtTokenValue()))
                                               .retrieve().body(new ParameterizedTypeReference<>() {
                });
        model.addAttribute("garages", garages);
        return "garage/garages";
    }
    
}
