package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner.GetImmatriculationDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner.VehiculeThymConvertDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static fr.cdrochon.thymeleaffrontend.security.SecurityConfigThymeleaf.getJwtTokenValue;


@Controller
@Slf4j
public class SearchVehiculeThymController {
    
    @Autowired
    private WebClient webClient;
    
    /**
     * Affiche le formulaire de recherche en interne d'un véhicule par son numéro d'immatriculation
     *
     * @param model Model pour les données à afficher
     * @return la vue searchVehiculeForm
     */
    @GetMapping("/showvehicule")
    @PreAuthorize("hasAuthority('USER')")
    public String searchVehiculeForm(Model model) {
        if(!model.containsAttribute("getImmatDTO")) {
            model.addAttribute("getImmatDTO", new GetImmatriculationDTO());
        }
        return "vehicule/inner/searchVehiculeForm";
    }
    
    /**
     * Recherche asynchrone d'un véhicule dans la bdd avec son numéro d'immatriculation
     *
     * @param getImmatDTO DTO correspondant à l'entité d'un véhicule.
     * @param result      BindingResult pour la validation des données
     * @param model       Model pour les données à afficher
     * @return la vue searchVehiculeForm
     */
    @GetMapping("/searchvehicule")
    @PreAuthorize("hasAuthority('USER')")
    public Mono<String> searchVehiculeAsync(@Valid @ModelAttribute("getImmatDTO") GetImmatriculationDTO getImmatDTO, BindingResult result,
                                            Model model) {
        if(result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));
            model.addAttribute("getImmatDTO", getImmatDTO);
            return Mono.just("vehicule/inner/searchVehiculeForm");
        }
        
        return webClient.get()
                        .uri("/queries/vehicules/immatriculation/" + getImmatDTO.getImmatriculation())
                        .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Content-Type", "application/json")
                        .retrieve()
                        .onStatus(HttpStatus.NOT_FOUND::equals, e -> Mono.empty())
                        .onStatus(HttpStatusCode::is5xxServerError,
                                  clientResponse -> Mono.error(new WebClientResponseException("Erreur interne du serveur",
                                                                                              500,
                                                                                              "Erreur de recherche du véhicule",
                                                                                              null,
                                                                                              null,
                                                                                              null)))
                        .bodyToMono(VehiculeThymConvertDTO.class)
                        .flatMap(vehicule -> {
                            // Ajout des attributs au modèle en cas de succès
                            model.addAttribute("vehicule", vehicule);
                            return Mono.just("vehicule/view");
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            model.addAttribute("errorMessage", "Véhicule non trouvé pour l'immatriculation '" + getImmatDTO.getImmatriculation() + "'");
                            model.addAttribute("alertClass", "alert-danger");
                            model.addAttribute("urlRedirection", "/showvehicule");
                            return Mono.just("error");
                        }))
                        .onErrorResume(e -> {
                            model.addAttribute("errorMessage", e.getMessage());
                            model.addAttribute("alertClass", "alert-danger");
                            model.addAttribute("urlRedirection", "/showvehicule");
                            return Mono.just("error");
                        });
    }
}
