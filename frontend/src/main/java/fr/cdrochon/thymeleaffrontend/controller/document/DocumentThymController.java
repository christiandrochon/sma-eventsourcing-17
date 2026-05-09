package fr.cdrochon.thymeleaffrontend.controller.document;

import fr.cdrochon.thymeleaffrontend.dtos.document.DocumentConvertThymDTO;
import fr.cdrochon.thymeleaffrontend.logging.FrontendLoggers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Controller
public class DocumentThymController {

    @Autowired
    private WebClient webClient;

    /**
     * Affiche la vue de création d'un document, avec eventuellement les details dejà saisis par l'user en cas d'erreur
     *
     * @param id                 id du document
     * @param model              modele de la vue
     * @return la vue de création d'un document
     */
    @GetMapping(value = "/document/{id}")
    public Mono<String> getDocumentByIdAsync(@PathVariable String id, Model model) {
        return webClient.get()
                        .uri("/queries/documents/" + id)
                        .retrieve()
                        .bodyToMono(DocumentConvertThymDTO.class)
                        .flatMap(dto -> {
                            assert dto != null;
                            model.addAttribute("document", dto);
                            return Mono.just("document/view");
                        })
                        .onErrorResume(WebClientResponseException.class, e -> {
                            FrontendLoggers.error().error("UI_DOCUMENT_READ_FAILED status={} message={}", e.getStatusCode(), e.getResponseBodyAsString());
                            model.addAttribute("alertClass", "alert-danger");
                            model.addAttribute("urlRedirection", "/documents");
                            model.addAttribute("errorMessage", "Erreur lors de la récupération du document (" + e.getStatusCode() + ").");
                            return Mono.just("error");
                        });
    }

    /**
     * Récupère la liste des documents
     *
     * @param model              Model
     * @return la vue des documents
     */
    @GetMapping(value = "/documents")
    public Mono<String> getDocumentsAsync(Model model) {
        return webClient.get()
                        .uri("/queries/documents")
                        .retrieve()
                        .bodyToFlux(DocumentConvertThymDTO.class)
                        .collectList()
                        .flatMap(documents -> {
                            assert documents != null;
                            model.addAttribute("documents", documents);
                            return Mono.just("document/documents");
                        })
                        .onErrorResume(WebClientResponseException.class, e -> {
                            FrontendLoggers.error().error("UI_DOCUMENT_LIST_FAILED status={} message={}", e.getStatusCode(), e.getResponseBodyAsString());
                            model.addAttribute("alertClass", "alert-danger");
                            model.addAttribute("urlRedirection", "/documents");
                            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                                model.addAttribute("errorMessage", "Accès interdit : vous n'avez pas les droits pour consulter les documents.");
                            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                                model.addAttribute("errorMessage", "Non authentifié : veuillez vous reconnecter.");
                            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                                model.addAttribute("errorMessage", "Aucun document trouvé.");
                            } else {
                                model.addAttribute("errorMessage", "Erreur inattendue (" + e.getStatusCode() + ").");
                            }
                            return Mono.just("error");
                        })
                        .onErrorResume(Exception.class, e -> {
                            FrontendLoggers.error().error("UI_DOCUMENT_LIST_UNEXPECTED_ERROR message={}", e.getMessage(), e);
                            model.addAttribute("alertClass", "alert-danger");
                            model.addAttribute("urlRedirection", "/documents");
                            model.addAttribute("errorMessage", "Erreur de connexion au serveur : " + e.getMessage());
                            return Mono.just("error");
                        });
    }
}
