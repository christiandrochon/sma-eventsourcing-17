package fr.cdrochon.thymeleaffrontend.controller.document;

import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentConvertThymDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
public class DocumentThymController {
    
    @Autowired
    private WebClient webClient;
    
    /**
     * Affiche la vue de création d'un document, avec eventuellement les details dejà saisis par l'user en cas d'erreur
     *
     * @param id                 id du document
     * @param model              modele de la vue
     * @param redirectAttributes attributs de redirection
     * @return la vue de création d'un document
     */
    @GetMapping(value = "/document/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public Mono<String> getDocumentByIdAsync(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        return webClient.get()
                        .uri("/queries/documents/" + id)
                        //                                  .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " +
                        //                                  getJwtTokenValue()))
                        .retrieve()
                        .bodyToMono(DocumentConvertThymDTO.class)
                        .flatMap(dto -> {
                            assert dto != null;
                            model.addAttribute("document", dto);
                            return Mono.just("document/view");
                        })
                        .onErrorResume(WebClientResponseException.class, e -> {
                            log.error("400 Bad Request: {}", e.getMessage());
                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                            redirectAttributes.addFlashAttribute("errorMessage", "Requête invalide.");
                            redirectAttributes.addFlashAttribute("urlRedirection", "/createDocument");
                            return Mono.just("redirect:/error");
                        });
    }
    
    /**
     * Récupère la liste des documents
     *
     * @param model              Model
     * @param redirectAttributes RedirectAttributes
     * @return la vue des documents
     */
    @GetMapping(value = "/documents")
    //    @PreAuthorize("hasAuthority('USER')")
    public Mono<String> getDocumentsAsync(Model model, RedirectAttributes redirectAttributes) {
        return webClient.get()
                        .uri("/queries/documents")
                        //                                         .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION,
                        //                                         "Bearer " + getJwtTokenValue()))
                        .retrieve()
                        .bodyToFlux(DocumentConvertThymDTO.class)
                        .collectList()
                        .flatMap(documents -> {
                            assert documents != null;
                            model.addAttribute("documents", documents);
                            return Mono.just("document/documents");
                        })
                        .onErrorResume(WebClientResponseException.class, e -> {
                            if(e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                                log.error("400 Bad Request: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Requête invalide.");
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDocument");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                                log.error("404 Not Found: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Ressource non trouvée. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDocument");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                                log.error("500 Internal Server Error: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage",
                                                                     "Erreur interne de serveur. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createDocument");
                                return Mono.just("redirect:/error");
                            }
                            log.error("ERREUR: {}", e.getResponseBodyAsString());
                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                            redirectAttributes.addFlashAttribute("errorMessage", "Erreur non reconnue. " + e.getResponseBodyAsString());
                            redirectAttributes.addFlashAttribute("urlRedirection", "/documents");
                            return Mono.just("redirect:/error");
                        });
    }
}
