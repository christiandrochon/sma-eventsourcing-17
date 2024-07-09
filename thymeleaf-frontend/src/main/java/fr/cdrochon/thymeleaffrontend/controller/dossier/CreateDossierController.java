package fr.cdrochon.thymeleaffrontend.controller.dossier;

import fr.cdrochon.thymeleaffrontend.dtos.client.AdresseClientDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.ClientPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.ClientStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.PaysDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentConvertPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.TypeDocumentDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierConvertPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeDateConvertDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculePostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeStatusDTO;
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
import java.util.Collection;
import java.util.List;

@Controller
public class CreateDossierController {
    final RestClient restClient = RestClient.create("http://localhost:8092");
    
    @GetMapping("/createDossier")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createDossier(Model model) {
        ClientPostDTO clientDTO = new ClientPostDTO();
        clientDTO.setAdresse(new AdresseClientDTO());
        
        if(!model.containsAttribute("dossierDTO")) {
            model.addAttribute("dossierDTO", new DossierPostDTO());
        }
        
        //chargement des listes de status de dossier, de status de vehicule, de status de client et de pays
        model.addAttribute("vehiculeStatuses", List.of(VehiculeStatusDTO.values()));
        model.addAttribute("dossierStatuses", List.of(DossierStatusDTO.values()));
        model.addAttribute("clientStatuses", List.of(ClientStatusDTO.values()));
        model.addAttribute("pays", List.of(PaysDTO.values()));
        return "dossier/createDossierForm";
    }
    
    @PostMapping(value = "/createDossier")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createDocument(@Valid @ModelAttribute("dossierDTO") DossierPostDTO dossierPostDTO, BindingResult result,
                                 RedirectAttributes redirectAttributes, Model model) {
        if(result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));
            // rechargement des listes en cas d'erreur du formulaire de création
            model.addAttribute("dossierDTO", dossierPostDTO);
            model.addAttribute("dossierStatuses", List.of(DossierStatusDTO.values()));
            model.addAttribute("vehiculeStatuses", List.of(VehiculeStatusDTO.values()));
            model.addAttribute("clientStatuses", List.of(ClientStatusDTO.values()));
            model.addAttribute("pays", List.of(PaysDTO.values()));
            return "dossier/createDossierForm";
        }
        try {
            DossierConvertPostDTO dossierConvertPostDTO = new DossierConvertPostDTO();
            
            dossierConvertPostDTO.setNomDossier(dossierPostDTO.getNomDossier());
            dossierConvertPostDTO.setDateCreationDossier(Instant.now());
            dossierConvertPostDTO.setDateModificationDossier(Instant.now());
            dossierConvertPostDTO.setDossierStatus(dossierPostDTO.getDossierStatus());
            dossierConvertPostDTO.setClient(dossierPostDTO.getClient());
            
            //conversion du vehciuel à cause la date de mise en circulation
            VehiculeDateConvertDTO vehiculeDateConvertDTO = new VehiculeDateConvertDTO();
            vehiculeDateConvertDTO.setImmatriculationVehicule(dossierPostDTO.getVehicule().getImmatriculationVehicule());
            vehiculeDateConvertDTO.setVehiculeStatus(dossierPostDTO.getVehicule().getVehiculeStatus());
            // conversion de la date de mise en circulation du vehicule
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String dateMiseEnCirculationVehicule = dossierPostDTO.getVehicule().getDateMiseEnCirculationVehicule();
            LocalDate localDate = LocalDate.parse(dateMiseEnCirculationVehicule, formatter);
            Instant date = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            vehiculeDateConvertDTO.setDateMiseEnCirculationVehicule(date);
            
            dossierConvertPostDTO.setVehicule(vehiculeDateConvertDTO);
            
            restClient.post().uri("/commands/createDossier")
                      //                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                      //                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(dossierConvertPostDTO).retrieve().toBodilessEntity();
            
            redirectAttributes.addFlashAttribute("successMessage", "Dossier created successfully");
            return "redirect:/dossiers";
            
        } catch(Exception e) {
            System.out.println("ERREUR DANS LE FOR DOCUMENT : " + e.getMessage());
            
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("documentDTO", dossierPostDTO); // Re-add garageDTO to the model if there's an error
            return "redirect:/createDossier";
        }
    }
}
