package fr.cdrochon.thymeleaffrontend.controller.dossier;

import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentConvertPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.TypeDocumentDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierConvertPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierStatusDTO;
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
        if(!model.containsAttribute("dossierDTO")) {
            model.addAttribute("dossierDTO", new DossierPostDTO());
        }
        
        //chargement des listes de type de document et de status de document
//        model.addAttribute("typeDocuments", TypeDocumentDTO.PREDEFINED_VALUES);
        model.addAttribute("dossierStatuses", List.of(DossierStatusDTO.values()));
        
        return "dossier/createDossierForm";
    }
    
    @PostMapping(value = "/createDossier")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createDocument(@Valid @ModelAttribute("dossierDTO") DossierPostDTO dossierPostDTO, BindingResult result,
                                 RedirectAttributes redirectAttributes, Model model) {
        if(result.hasErrors()) {
            model.addAttribute("dossierDTO", dossierPostDTO);
            // rechargement des listes en cas d'erreur du formulaire de création
            model.addAttribute("dossierStatuses", List.of(DossierStatusDTO.values()));
            return "dossier/createDossierForm";
        }
        try {
            DossierConvertPostDTO dossierConvertPostDTO = new DossierConvertPostDTO();
            dossierConvertPostDTO.setId(dossierPostDTO.getId());
            dossierConvertPostDTO.setNomDossier(dossierPostDTO.getNomDossier());
            
            //Transformation du String en Instant
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            String dateCreationDossier = dossierPostDTO.getDateCreationDossier();
            LocalDate localDate = LocalDate.parse(dateCreationDossier, formatter);
            Instant dateCreation = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            dossierConvertPostDTO.setDateCreationDossier(dateCreation);
            
            String dateModificationDossier = dossierPostDTO.getDateModificationDossier();
            LocalDate localDate1 = LocalDate.parse(dateModificationDossier, formatter);
            Instant dateModif = localDate1.atStartOfDay().toInstant(ZoneOffset.UTC);
            dossierConvertPostDTO.setDateModificationDossier(dateModif);
            
            dossierConvertPostDTO.setClient(dossierPostDTO.getClient());
            dossierConvertPostDTO.setVehicule(dossierPostDTO.getVehicule());
            dossierConvertPostDTO.setDossierStatus(dossierPostDTO.getDossierStatus());
            
            restClient.post().uri("/commands/createDossier")
                      //                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                      //                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(dossierConvertPostDTO).retrieve().toBodilessEntity();
            
            redirectAttributes.addFlashAttribute("successMessage", "Dossier created successfully");
            return "redirect:/dossiers";
        } catch(Exception e) {
            System.out.println("ERREUR DANS LE FOR DOCUMENT : " + e.getMessage());
            //            return new ModelAndView("createGarageForm");
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("documentDTO", dossierPostDTO); // Re-add garageDTO to the model if there's an error
            //            return new ModelAndView("redirect:/createGarage");
            return "redirect:/createDossier";
        }
    }
}
