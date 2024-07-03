package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.entity.vehicule.Vehicule;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;

import java.util.List;

@Controller
public class VehiculeController {
    
    RestClient restClient = RestClient.create("http://localhost:8092");
    @GetMapping("/vehicule/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public String vehiculeById(@PathVariable Long id, Model model) {
        Vehicule vehicule = restClient.get().uri("/queries/vehicules/" + id)
                                      //                                      .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                                      .retrieve().body(new ParameterizedTypeReference<>() {
                });
        model.addAttribute("vehicule", vehicule);
        return "vehicule/view";
    }
    
    @GetMapping("/vehicules")
    //    @PreAuthorize("hasAuthority('USER')")
    public String vehicules(Model model) {
        List<Vehicule> vehicules = restClient.get().uri("/queries/vehicules")
                                             //                                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                                             .retrieve().body(new ParameterizedTypeReference<>() {
                });
        model.addAttribute("vehicules", vehicules);
        return "vehicule/vehicules";
    }
}
