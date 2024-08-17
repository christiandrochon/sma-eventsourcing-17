package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeDateConvertDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculePostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeStatusDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@Slf4j
public class CreateVehiculeController {
    
    final RestClient restClient = RestClient.create("http://localhost:8092");
    
    /**
     * Affiche le formulaire de création d'un vehicule
     *
     * @param model modèle du vehicule: permet de passer des attributs à la vue
     * @return la vue createVehiculeForm
     */
    @GetMapping("/createVehicule")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createVehicule(Model model) {
        if(!model.containsAttribute("vehiculeDTO")) {
            model.addAttribute("vehiculePostDTO", new VehiculePostDTO());
        }
        //chargement des listes de type de document et de status de vehicule
        model.addAttribute("vehiculeStatuses", List.of(VehiculeStatusDTO.values()));
        return "vehicule/createVehiculeForm";
    }
    
    /**
     * Création d'un vehicule via un formulaire.
     * <p></p>
     * Vérifie si l'immatriculation existe déjà.
     * Vérifie si une erreur est survenue lors de la création du vehicule.
     * Vérifie si la date de mise en circulation est valide.
     * Vérifie si le status du vehicule est valide.
     * Vérifie si le vehicule a bien été créé.
     *
     * @param vehiculePostDTO    DTO du vehicule à créer : permet de récupérer les données du formulaire
     * @param result             BindingResult pour la validation du formulaire: permet de tester les erreurs de validation
     * @param redirectAttributes RedirectAttributes : permet de passer des attributs à la redirection
     * @param model              modèle pour la vue :   permet de passer des attributs à la vue
     * @return la vue createVehiculeForm
     */
    @PostMapping(value = "/createVehicule")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createVehicule(@Valid @ModelAttribute("vehiculePostDTO") VehiculePostDTO vehiculePostDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        
        if(result.hasErrors()) {
            log.error("LOG ERROR : {}", result.getAllErrors());
            model.addAttribute("vehiculePostDTO", vehiculePostDTO);
            model.addAttribute("vehiculeStatuses", List.of(VehiculeStatusDTO.values()));
            //ce return conserve l'etat du form et permet de reafficher les erreurs de validation courantes
            return "vehicule/createVehiculeForm";
        }
        try {
            
            if(immatriculationExiste(vehiculePostDTO.getImmatriculationVehicule()) && vehiculePostDTO.getImmatriculationVehicule() != null) {
                // Numéro d'immatriculation déjà existant
                model.addAttribute("vehiculePostDTO", vehiculePostDTO);
                model.addAttribute("vehiculeStatuses", List.of(VehiculeStatusDTO.values()));
                model.addAttribute("immatriculationExisteError",
                                   "L'immatriculation existe déjà, vous ne pouvez pas créer deux véhicules ayant la même immatriculation.");
                return "vehicule/createVehiculeForm";
            }
            
            VehiculeDateConvertDTO vehiculeDateConvertDTO = new VehiculeDateConvertDTO();
            
            vehiculeDateConvertDTO.setImmatriculationVehicule(vehiculePostDTO.getImmatriculationVehicule());
            //Transformation du String en Instant
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(vehiculePostDTO.getDateMiseEnCirculationVehicule(), formatter);
            Instant instant = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            vehiculeDateConvertDTO.setDateMiseEnCirculationVehicule(instant);
            
            vehiculeDateConvertDTO.setVehiculeStatus(vehiculePostDTO.getVehiculeStatus());
            
            //TODO : rendre aync. Attention, lors du debug, la liste des vehicules n'est pas à jour lorsque je cree un nouveau vehicule. Mais en mlode
            // normal, c'est ok
            restClient.post().uri("/commands/createVehicule")
                      //                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                      //                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(vehiculeDateConvertDTO)
                      .retrieve()
                      .toBodilessEntity();
            
            redirectAttributes.addFlashAttribute("successMessage", "Vehicule créé !");
            return "redirect:/vehicules";
        } catch(Exception e) {
            log.error("An error occurred: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("vehiculePostDTO", vehiculePostDTO); // Re-add vehiculeDTO to the model if there's an error
            return "redirect:/createVehicule";
        }
    }
    
    /**
     * Empêche la création d'un vehicule si l'immatriculation existe déjà.
     *
     * @param immatriculation immatriculation du vehicule
     * @return Boolean
     */
    Boolean immatriculationExiste(@PathVariable String immatriculation) {
        return restClient.get()
                         .uri("/queries/vehiculeExists/" + immatriculation)
                         .accept(MediaType.APPLICATION_JSON)
                         .retrieve()
                         .body(Boolean.class);
    }
}
