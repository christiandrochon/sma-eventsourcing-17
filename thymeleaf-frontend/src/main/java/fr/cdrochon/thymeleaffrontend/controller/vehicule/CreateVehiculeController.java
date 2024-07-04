package fr.cdrochon.thymeleaffrontend.controller.vehicule;

import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculePostDTO;
import fr.cdrochon.thymeleaffrontend.entity.vehicule.Vehicule;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.*;
import java.time.format.DateTimeFormatter;

@Controller
public class CreateVehiculeController {
    
    RestClient restClient = RestClient.create("http://localhost:8092");
    
    @GetMapping("/createVehicule")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createVehicule(Model model) {
        if(!model.containsAttribute("vehiculeDTO")) {
            model.addAttribute("vehiculePostDTO", new VehiculePostDTO());
        }
        return "vehicule/createVehiculeForm";
    }
    
    @PostMapping(value = "/createVehicule")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarage(@Valid @ModelAttribute VehiculePostDTO vehiculePostDTO, BindingResult result, RedirectAttributes redirectAttributes,
                               Model model) {
        if(result.hasErrors()) {
            model.addAttribute("vehiculePostDTO", vehiculePostDTO);
            //            return new ModelAndView("createGarageForm");
            return "/vehicule/createVehiculeForm";
        }
        try {
            Vehicule vehicule = new Vehicule();
            vehicule.setImmatriculationVehicule(vehiculePostDTO.getImmatriculationVehicule());
            //Transformation du String en Instant
            String dateMiseEnCirculationVehicule = vehiculePostDTO.getDateMiseEnCirculationVehicule().toString();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(dateMiseEnCirculationVehicule, formatter);
            Instant instant = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
//            Instant instant = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            vehicule.setDateMiseEnCirculationVehicule(instant);
            
            restClient.post().uri("/commands/createVehicule")
                      //                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                      //                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(vehicule).retrieve().toBodilessEntity();
            
            //            System.out.println(responseEntity);
            //            return new ModelAndView("redirect:/garages");
            return "redirect:/vehicules";
        } catch(Exception e) {
            System.out.println("VEHICULE THYMELEAF ERRRRRRRRRRRRRRRRROOR : " + e.getMessage());
            //            return new ModelAndView("createGarageForm");
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("vehiculePostDTO", vehiculePostDTO); // Re-add garageDTO to the model if there's an error
            //            return new ModelAndView("redirect:/createGarage");
            return "redirect:/createVehicule";
        }
    }
    
    /**
     * Convert a string LocalDate to an Instant
     *
     * @param date the date to convert
     * @return the converted date
     */
    public Instant convertLocalDateToInstant(Instant date) {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); //ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String formattedDateTime = dateTime.format(formatter); // "2023-01-28T15:20:45"
        System.out.println(formattedDateTime);
        return Instant.parse(formattedDateTime);
    }
    
    
    /**
     * Convert a string date to an Instant
     *
     * @param date
     * @return
     */
    private Instant convertStringToInstant(String date) {
        return Instant.parse(date);
    }
}
