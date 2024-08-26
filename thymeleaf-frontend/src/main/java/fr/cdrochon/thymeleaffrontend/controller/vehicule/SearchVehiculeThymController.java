package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner.GetImmatriculationDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.inner.VehiculeThymDTO;
import fr.cdrochon.thymeleaffrontend.exception.InvalidDateFormatException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.Date;


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
    public Mono<String> searchVehiculeAsync(@Valid @ModelAttribute("getImmatDTO") GetImmatriculationDTO getImmatDTO, BindingResult result,
                                            Model model) {
        if(result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));
            model.addAttribute("getImmatDTO", getImmatDTO);
            return Mono.just("vehicule/inner/searchVehiculeForm");
        }
        
        return webClient.get()
                        .uri("/queries/vehicules/immatriculation/" + getImmatDTO.getImmatriculation())
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Content-Type", "application/json")
                        .retrieve()
                        .onStatus(HttpStatus.NOT_FOUND::equals,
                                  clientResponse -> Mono.empty())
                        .onStatus(HttpStatusCode::is5xxServerError,
                                  clientResponse -> Mono.error(new WebClientResponseException("Erreur interne du serveur",
                                                                                              500,
                                                                                              "Erreur de recherche du véhicule",
                                                                                              null,
                                                                                              null,
                                                                                              null)))
                        .bodyToMono(VehiculeThymDTO.class)
                        .flatMap(vehicule -> {
                            
                            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy");
                            try {
                                Date date = inputFormat.parse(vehicule.getDateMiseEnCirculationVehicule());
                                String dateMiseEnCirculationVehicule = outputFormat.format(date);
                                vehicule.setDateMiseEnCirculationVehicule(dateMiseEnCirculationVehicule);
                            } catch(ParseException e) {
                                return Mono.error(new InvalidDateFormatException("Erreur de conversion de date. Le format attendu est de type 'dd MMMM " +
                                                                                         "yyyy'"));
                            }
                            
                            // Ajout des attributs au modèle en cas de succès
                            model.addAttribute("vehicule", vehicule);
                            return Mono.just("vehicule/inner/resultSearchVehiculeView");
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
