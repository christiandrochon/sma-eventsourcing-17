package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeDateConvertDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;

import java.util.List;

@Controller
public class VehiculeController {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    
    private final RestClient restClient;
    public VehiculeController(RestClient restClient) {
        this.restClient = restClient;
    }
    
    @GetMapping("/vehicule/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public String vehiculeById(@PathVariable String id, Model model) {
        VehiculeDateConvertDTO vehicule = restClient.get().uri(externalServiceUrl + "/queries/vehicules/" + id)
                                                    //                                      .headers(httpHeaders -> httpHeaders.set(HttpHeaders
                                                    //                                      .AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                                                    .retrieve().body(new ParameterizedTypeReference<>() {
                });
        model.addAttribute("vehicule", vehicule);
        return "vehicule/view";
    }
    
    //TODO : rendre aync. Attention, lors du debug, la liste des vehicules n'est pas à jour lorsque je cree un nouveau vehicule. Mais en mlode normal, c'est ok
    @GetMapping("/vehicules")
    //    @PreAuthorize("hasAuthority('USER')")
    public String vehicules(Model model) {
        List<VehiculeDateConvertDTO> vehicules = restClient.get().uri(externalServiceUrl + "/queries/vehicules")
                                                           //.headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                                                           .retrieve().body(new ParameterizedTypeReference<>() {
                });
        model.addAttribute("vehicules", vehicules);
        return "vehicule/vehicules";
    }
}
