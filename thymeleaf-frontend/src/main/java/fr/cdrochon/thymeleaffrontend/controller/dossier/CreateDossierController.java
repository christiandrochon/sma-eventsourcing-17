package fr.cdrochon.thymeleaffrontend.controller.dossier;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.client.PaysDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierConvertPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeDateConvertDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeStatusDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@Slf4j
public class CreateDossierController {
    @Autowired
    private WebClient webClient;
    final RestClient restClient = RestClient.create("http://localhost:8092");
    
    @GetMapping("/createDossier")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createDossier(Model model) {
        //initilaisation du client
        //        ClientPostDTO clientDTO = new ClientPostDTO();
        //        clientDTO.setAdresse(new AdresseClientDTO());
        
        
        if(!model.containsAttribute("dossierDTO")) {
            model.addAttribute("dossierDTO", new DossierPostDTO());
        }
        
        //chargement des listes de status de dossier, de status de vehicule, de status de client et de pays
        model.addAttribute("dossierStatuses", List.of(DossierStatusDTO.values()));
        model.addAttribute("vehiculeStatuses", List.of(VehiculeStatusDTO.values()));
        model.addAttribute("clientStatuses", List.of(ClientStatusDTO.values()));
        model.addAttribute("valeurDossierStatutParDefaut", DossierStatusDTO.valeurDossierStatutParDefaut());
        
        model.addAttribute("paysList", List.of(PaysDTO.values()));
        model.addAttribute("valeurPaysParDefaut", PaysDTO.valeurPaysParDefaut());
        
        return "dossier/createDossierForm";
    }
    
    @PostMapping(value = "/createDossier")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createDocument(@Valid @ModelAttribute("dossierDTO") DossierPostDTO dossierPostDTO, BindingResult result,
                                 RedirectAttributes redirectAttributes, Model model) {
        if(result.hasErrors()) {
            result.getAllErrors().forEach(err -> log.error("LOG ERROR : {}", err.getDefaultMessage()));
            // rechargement des listes en cas d'erreur du formulaire de création
            model.addAttribute("dossierStatuses", List.of(DossierStatusDTO.values()));
            model.addAttribute("vehiculeStatuses", List.of(VehiculeStatusDTO.values()));
            model.addAttribute("clientStatuses", List.of(ClientStatusDTO.values()));
            model.addAttribute("paysList", List.of(PaysDTO.values()));
            model.addAttribute("dossierDTO", dossierPostDTO);
            return "dossier/createDossierForm";
        }
        try {
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
            
            //conversion du dossier
            DossierConvertPostDTO dossierConvertPostDTO = new DossierConvertPostDTO();
            dossierConvertPostDTO.setNomDossier(dossierPostDTO.getNomDossier());
            dossierConvertPostDTO.setDateCreationDossier(Instant.now());
            dossierConvertPostDTO.setDateModificationDossier(Instant.now());
            dossierConvertPostDTO.setDossierStatus(dossierPostDTO.getDossierStatus());
            dossierConvertPostDTO.setClient(dossierPostDTO.getClient());
            dossierConvertPostDTO.setVehicule(vehiculeDateConvertDTO);
            
            //appel du ms dossier pour la création du dossier
            webClient.post()
                     .uri("/commands/createDossier")
                     .contentType(MediaType.APPLICATION_JSON)
                     .bodyValue(dossierConvertPostDTO)
                     .retrieve()
                     .bodyToMono(String.class)
                     .block();

            log.info("Dossier created successfully");
            redirectAttributes.addFlashAttribute("successMessage", "Dossier créé avec succès");
            redirectAttributes.addFlashAttribute("urlRedirection", "/dossiers");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
            return "redirect:/success";
//            return "redirect:/dossiers";
            
        } catch(Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                                                 "Erreur de création de dossier. Merci de communiquer le contenu de l'erreur suivante au développeur : '" + e.getMessage() + "'");
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
            redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
            redirectAttributes.addFlashAttribute("dossierDTO", dossierPostDTO); // Re-add garageDTO to the model if there's an error
//            return "redirect:/createDossier";
            return "redirect:/error";
        
        }
    }
    
    /**
     * Affiche la page de succès
     * @return la vue success
     */
    @GetMapping("/success")
    public String showSuccess(){
        return "success";
    }
}
