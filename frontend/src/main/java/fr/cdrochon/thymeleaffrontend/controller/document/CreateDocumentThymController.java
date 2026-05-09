package fr.cdrochon.thymeleaffrontend.controller.document;

import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentConvertThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentStatusDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.document.TypeDocumentDTO;
import jakarta.validation.Valid;
import fr.cdrochon.thymeleaffrontend.logging.FrontendLoggers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static fr.cdrochon.thymeleaffrontend.formatdata.ConvertObjectToJson.convertObjectToJson;

@Controller
public class CreateDocumentThymController {
    
    @Autowired
    private WebClient webClient;
    
    /**
     * Affiche le formulaire de création de document, ou réaffiche les données saisies du formulaire en cas d'erreur de validation
     *
     * @param model Model contenant les données à afficher dans la vue
     * @return Vue de création de document
     */
    @GetMapping("/createDocument")
    public String getDocument(Model model) {
        if(!model.containsAttribute("documentDTO")) {
            model.addAttribute("documentDTO", new DocumentThymDTO());
        }
        
        //chargement des listes de type de document et de status de document
        model.addAttribute("typeDocuments", TypeDocumentDTO.PREDEFINED_VALUES);
        model.addAttribute("documentStatuses", List.of(DocumentStatusDTO.values()));
        return "document/createDocumentForm";
    }
    
    /**
     * Création asynchrone d'un document
     *
     * @param documentThymDTO    DTO de création de document
     * @param result             BindingResult contenant les erreurs de validation
     * @param redirectAttributes RedirectAttributes pour les messages de succès ou d'erreur
     * @param model              Model contenant les données à afficher dans la vue
     * @return Vue de création de document ou redirection vers la liste des documents
     */
    @PostMapping(value = "/createDocument")
    public Mono<String> createDocumentAsync(@Valid @ModelAttribute("documentDTO") DocumentThymDTO documentThymDTO, BindingResult result,
                                            RedirectAttributes redirectAttributes, Model model) {
        if(result.hasErrors()) {
            result.getAllErrors().forEach(err -> FrontendLoggers.error().warn("UI_DOCUMENT_CREATE_VALIDATION_ERROR field={}", err.getDefaultMessage()));
            model.addAttribute("documentDTO", documentThymDTO);
            // rechargement des listes en cas d'erreur du formulaire de création
            Collection<TypeDocumentDTO> typeDocuments = TypeDocumentDTO.PREDEFINED_VALUES;
            model.addAttribute("typeDocuments", typeDocuments);
            model.addAttribute("documentStatuses", List.of(DocumentStatusDTO.values()));
            return Mono.just("document/createDocumentForm");
        }
        
         //convertir les date  String > Instant
         DocumentConvertThymDTO documentConvertThymDTO = convertThymDTO(documentThymDTO);
         String jsonPayload = convertObjectToJson(documentConvertThymDTO);
         FrontendLoggers.access().info("UI_DOCUMENT_CREATE_REQUEST nomDocument={} titre={} emetteur={} type={} status={} payload={}",
                                       documentThymDTO.getNomDocument(), documentThymDTO.getTitreDocument(),
                                       documentThymDTO.getEmetteurDuDocument(), documentThymDTO.getTypeDocument(),
                                       documentThymDTO.getDocumentStatus(), jsonPayload);
         FrontendLoggers.business().info("UI_DOCUMENT_CREATE_REQUEST nomDocument={} type={} status={}",
                                         documentThymDTO.getNomDocument(), documentThymDTO.getTypeDocument(), documentThymDTO.getDocumentStatus());

        return webClient.post()
                        .uri("/commands/createDocument")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(jsonPayload)
                        .retrieve()
                        .bodyToMono(DocumentConvertThymDTO.class)
                        .timeout(Duration.ofSeconds(3000))
                        .flatMap(doc -> {
                            if(doc == null || doc.getId() == null) {
                                FrontendLoggers.error().error("UI_DOCUMENT_CREATE_FAILED message=response null or missing id");
                                return Mono.error(new RuntimeException("Erreur lors de la création du document"));
                            }
                            
                            FrontendLoggers.access().info("UI_DOCUMENT_CREATE_RESPONSE doc={}", doc);
                            FrontendLoggers.access().info("UI_DOCUMENT_CREATE_DATE_TYPE type={}", doc.getDateCreationDocument().getClass());
                            FrontendLoggers.access().info("UI_DOCUMENT_CREATE_OK");
                            FrontendLoggers.business().info("UI_DOCUMENT_CREATE_OK documentId={} nomDocument={} type={}",
                                                            doc.getId(), doc.getNomDocument(), doc.getTypeDocument());
                            redirectAttributes.addFlashAttribute("successMessage", "Dossier créé avec succès");
                            return Mono.just("redirect:/document/" + doc.getId());
                            // redirection vers la liste des clients
                            //                            redirectAttributes.addFlashAttribute("successMessage", "Client créé avec succès");
                            //                            return Mono.just("redirect:/documents/" );
                        })
                        .onErrorResume(TimeoutException.class, e -> {
                            FrontendLoggers.error().error("UI_DOCUMENT_CREATE_FAILED reason=timeout message={}", e.getMessage());
                            FrontendLoggers.business().error("UI_DOCUMENT_CREATE_FAILED reason=timeout nomDocument={} message={}",
                                                             documentThymDTO.getNomDocument(), e.getMessage());
                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                            redirectAttributes.addFlashAttribute("errorMessage", "Tempes de requête dépassé. Veuillez réessayer plus tard.");
                            redirectAttributes.addFlashAttribute("urlRedirection", "/createDocument");
                            return Mono.just("redirect:/error");
                        })
                        .onErrorResume(WebClientResponseException.class, e -> {
                            FrontendLoggers.business().error("UI_DOCUMENT_CREATE_FAILED status={} nomDocument={} message={}",
                                                             e.getStatusCode().value(), documentThymDTO.getNomDocument(), e.getResponseBodyAsString());
                            if(e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                                FrontendLoggers.error().error("UI_DOCUMENT_CREATE_FAILED status=400 message={}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Requête invalide.");
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDocument");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                                FrontendLoggers.error().error("UI_DOCUMENT_CREATE_FAILED status=401 message={}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Accès non autorisé. " + e
                                        .getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDocument");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.FORBIDDEN) {
                                FrontendLoggers.error().error("UI_DOCUMENT_CREATE_FAILED status=403 message={}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Accès interdit. " + e
                                        .getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDocument");
                                return Mono.just("redirect:/error");
                                
                            } else if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                                FrontendLoggers.error().error("UI_DOCUMENT_CREATE_FAILED status=404 message={}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Ressource non trouvée. " + e
                                        .getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.UNSUPPORTED_MEDIA_TYPE) {
                                FrontendLoggers.error().error("UI_DOCUMENT_CREATE_FAILED status=415 message={}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage",
                                                                     "Type de média non supporté. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDocument");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.LOCKED) {
                                FrontendLoggers.error().error("UI_DOCUMENT_CREATE_FAILED status=423 message={}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage",
                                                                     "Ressource verrouillée. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDocument");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                                FrontendLoggers.error().error("UI_DOCUMENT_CREATE_FAILED status=500 message={}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage",
                                                                     "Erreur interne de serveur. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDossier");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                                FrontendLoggers.error().error("UI_DOCUMENT_CREATE_FAILED status=503 message={}", e.getMessage());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Service indisponible : " + e.getMessage());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDocument");
                                return Mono.just("redirect:/error");
                            }
                            // reaffiche le formualire de création de client avec les données saisies par l'user
                            redirectAttributes.addFlashAttribute("documentDTO", documentThymDTO);
                            return Mono.just("redirect:/createDocument");
                        });
    }
    
    /**
     * Convertit un DocumentThymDTO en DocumentConvertThymDTO
     *
     * @param documentThymDTO DocumentThymDTO à convertir
     * @return DocumentConvertThymDTO converti
     */
    private DocumentConvertThymDTO convertThymDTO(DocumentThymDTO documentThymDTO) {
        try {
            DocumentConvertThymDTO document = new DocumentConvertThymDTO();
            document.setId(documentThymDTO.getId());
            document.setNomDocument(documentThymDTO.getNomDocument());
            document.setTitreDocument(documentThymDTO.getTitreDocument());
            document.setEmetteurDuDocument(documentThymDTO.getEmetteurDuDocument());
            document.setTypeDocument(documentThymDTO.getTypeDocument());
            
            //reconvertir la date de mise en circulation du véhicule en Instant , d'abord en LocalDate de type 'yyyy-MM-dd', puis réellement en Instant
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(documentThymDTO.getDateCreationDocument(), formatter);
            Instant instant = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            document.setDateCreationDocument(instant);
            
            LocalDate localDate1 = LocalDate.parse(documentThymDTO.getDateModificationDocument(), formatter);
            Instant instant1 = localDate1.atStartOfDay().toInstant(ZoneOffset.UTC);
            document.setDateModificationDocument(instant1);
            
            document.setDocumentStatus(documentThymDTO.getDocumentStatus());
            return document;
        } catch(Exception e) {
            FrontendLoggers.error().error("UI_DOCUMENT_CONVERT_FAILED message={}", e.getMessage());
            throw new RuntimeException("Erreur lors de la conversion du document");
        }
    }
}
