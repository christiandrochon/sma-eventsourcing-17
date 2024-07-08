package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculePostDTO;
import fr.cdrochon.thymeleaffrontend.entity.document.DocumentStatus;
import fr.cdrochon.thymeleaffrontend.entity.vehicule.Vehicule;
import fr.cdrochon.thymeleaffrontend.entity.vehicule.VehiculeStatus;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class CreateVehiculeController {
    
    final RestClient restClient = RestClient.create("http://localhost:8092");
    
    @GetMapping("/createVehicule")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createVehicule(Model model) {
        if(!model.containsAttribute("vehiculeDTO")) {
            model.addAttribute("vehiculePostDTO", new VehiculePostDTO());
        }
        //chargement des listes de type de document et de status de vehicule
        model.addAttribute("vehiculeStatuses", List.of(VehiculeStatus.values()));
        return "vehicule/createVehiculeForm";
    }
    
    @PostMapping(value = "/createVehicule")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createVehicule(@Valid @ModelAttribute("vehiculePostDTO") VehiculePostDTO vehiculePostDTO, BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if(result.hasErrors()) {
            
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));
            model.addAttribute("vehiculePostDTO", vehiculePostDTO);
            model.addAttribute("vehiculeStatuses", List.of(VehiculeStatus.values()));
            return "vehicule/createVehiculeForm";
        }
        try {
            Vehicule vehicule = new Vehicule();
            vehicule.setImmatriculationVehicule(vehiculePostDTO.getImmatriculationVehicule());

            //Transformation du String en Instant
            String dateMiseEnCirculationVehicule = vehiculePostDTO.getDateMiseEnCirculationVehicule();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(dateMiseEnCirculationVehicule, formatter);
            Instant instant = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            //            Instant instant = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            vehicule.setDateMiseEnCirculationVehicule(instant);

            vehicule.setVehiculeStatus(vehiculePostDTO.getVehiculeStatus());
            
            restClient.post().uri("/commands/createVehicule")
                      //                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                      //                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(vehicule).retrieve().toBodilessEntity();
            redirectAttributes.addFlashAttribute("successMessage", "Vehicule is created !");
            return "redirect:/vehicules";
        } catch(Exception e) {
            System.out.println("VEHICULE THYMELEAF ERRRRRRRRRRRRRRRRROOR : " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("vehiculePostDTO", vehiculePostDTO); // Re-add vehiculeDTO to the model if there's an error
            return "redirect:/createVehicule";
        }
    }
}
