package fr.cdrochon.thymeleaffrontend.controller.dossier;

import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentConvertPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierConvertPostDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;

import java.util.List;

@Controller
public class DossierController {
    
    final RestClient restClient = RestClient.create("http://localhost:8092");
    @GetMapping("/dossier/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public String getDossierById(@PathVariable String id, Model model) {
        DossierConvertPostDTO dosssier = restClient.get().uri("/queries/dossiers/" + id)
                                                   //                                  .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                                                   .retrieve().body(new ParameterizedTypeReference<>() {
                });
        model.addAttribute("dossier", dosssier);
        return "dossier/view";
    }
    
    @GetMapping("/dossiers")
    //    @PreAuthorize("hasAuthority('USER')")
    public String getAllDocuments(Model model) {
        List<DossierConvertPostDTO> dossiers =
                restClient.get()
                          .uri("/queries/dossiers")
                          //                          .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                          .retrieve().body(new ParameterizedTypeReference<>() {
                          });
        model.addAttribute("dossiers", dossiers);
        return "dossier/dossiers";
    }
}
