package fr.cdrochon.thymeleaffrontend.controller.document;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentConvertPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentPostDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.TypeDocumentDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Slf4j
@Controller
public class CreateDocumentController {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    private final RestClient restClient;
    
    public CreateDocumentController(RestClient restClient) {
        this.restClient = restClient;
    }
    
    @GetMapping("/createDocument")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createDocument(Model model) {
        
        if(!model.containsAttribute("documentDTO")) {
            model.addAttribute("documentDTO", new DocumentPostDTO());
        }
        //chargement des listes de type de document et de status de document
        model.addAttribute("typeDocuments", TypeDocumentDTO.PREDEFINED_VALUES);
        model.addAttribute("documentStatuses", List.of(DocumentStatusDTO.values()));
        return "document/createDocumentForm";
    }
    
    @PostMapping(value = "/createDocument")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public String createDocument(@Valid @ModelAttribute("documentDTO") DocumentPostDTO documentPostDTO, BindingResult result,
                                 RedirectAttributes redirectAttributes, Model model) {
        if(result.hasErrors()) {
            model.addAttribute("documentDTO", documentPostDTO);
            // rechargement des listes en cas d'erreur du formulaire de création
            Collection<TypeDocumentDTO> typeDocuments = TypeDocumentDTO.PREDEFINED_VALUES;
            model.addAttribute("typeDocuments", typeDocuments);
            model.addAttribute("documentStatuses", List.of(DocumentStatusDTO.values()));
            return "document/createDocumentForm";
        }
        try {
            DocumentConvertPostDTO document = new DocumentConvertPostDTO();
            document.setId(documentPostDTO.getId());
            document.setNomDocument(documentPostDTO.getNomDocument());
            document.setTitreDocument(documentPostDTO.getTitreDocument());
            document.setEmetteurDuDocument(documentPostDTO.getEmetteurDuDocument());
            document.setTypeDocument(documentPostDTO.getTypeDocument());
            
            //Transformation du String en Instant
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            String dateCreationDocument = documentPostDTO.getDateCreationDocument();
            LocalDate localDate = LocalDate.parse(dateCreationDocument, formatter);
            Instant dateCreation = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            document.setDateCreationDocument(dateCreation);
            
            String dateModificationDocument = documentPostDTO.getDateModificationDocument();
            LocalDate localDate1 = LocalDate.parse(dateModificationDocument, formatter);
            Instant dateModif = localDate1.atStartOfDay().toInstant(ZoneOffset.UTC);
            document.setDateModificationDocument(dateModif);
            
            document.setDocumentStatus(documentPostDTO.getDocumentStatus());
            //TODO : requete async
            restClient.post().uri(externalServiceUrl + "/commands/createDocument")
                      //                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                      //                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                      .contentType(MediaType.APPLICATION_JSON)
                      .body(document).retrieve().toBodilessEntity();
            
            redirectAttributes.addFlashAttribute("successMessage", "Document created successfully");
            return "redirect:/documents";
        } catch(Exception e) {
            log.error("An error occurred: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            redirectAttributes.addFlashAttribute("documentDTO", documentPostDTO); // Re-add garageDTO to the model if there's an error
            return "redirect:/createDocument";
        }
    }
}
