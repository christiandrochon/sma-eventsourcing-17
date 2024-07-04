package fr.cdrochon.thymeleaffrontend.controller.document;

import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentPostDTO;
import fr.cdrochon.thymeleaffrontend.entity.document.Document;
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
@Controller
public class CreateDocumentController {
    
    RestClient restClient = RestClient.create("http://localhost:8092");
    
    @GetMapping("/createDocument")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarage(Model model) {
        if (!model.containsAttribute("documentDTO")) {
            model.addAttribute("documentDTO", new DocumentPostDTO());
        }
        return "document/createDocumentForm";
    }
    
    @PostMapping(value = "/createDocument")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarage(@Valid @ModelAttribute("documentDTO") DocumentPostDTO documentPostDTO, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if(result.hasErrors()) {
            model.addAttribute("documentDTO", documentPostDTO);
            return "document/createDocumentForm";
        }
        try {
            Document document = new Document();
            document.setNomDocument(documentPostDTO.getNomDocument());
            document.setTitreDocument(documentPostDTO.getTitreDocument());
            document.setEmetteurDuDocument(documentPostDTO.getEmetteurDuDocument());
            document.setTypeDocument(documentPostDTO.getTypeDocument());
            document.setDateCreationDocument(documentPostDTO.getDateCreationDocument());
            document.setDateModificationDocument(documentPostDTO.getDateModificationDocument());
            document.setDocumentStatus(documentPostDTO.getDocumentStatus());
            
            restClient.post().uri("/commands/createDocument")
                      //                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                      //                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(document).retrieve().toBodilessEntity();

            return "redirect:/documents";
        } catch(Exception e) {
            System.out.println("ERREUR DANS LE FOR DOCUMENT : " + e.getMessage());
            //            return new ModelAndView("createGarageForm");
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("documentDTO", documentPostDTO); // Re-add garageDTO to the model if there's an error
            //            return new ModelAndView("redirect:/createGarage");
            return "redirect:/createDocument";
        }
    }
}
