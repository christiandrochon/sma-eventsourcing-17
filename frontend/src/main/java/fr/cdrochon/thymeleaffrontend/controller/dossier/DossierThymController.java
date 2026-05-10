package fr.cdrochon.thymeleaffrontend.controller.dossier;

import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierThymConvertDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierThymDTO;
import fr.cdrochon.thymeleaffrontend.logging.FrontendLoggers;
import fr.cdrochon.thymeleaffrontend.security.FrontendTokenResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Controller
public class DossierThymController {
    
    @Autowired
    private WebClient webClient;

    @Autowired
    private FrontendTokenResolver tokenResolver;

    /**
     * Affiche les détails d'un dossier spécifique, ou renvoi les informations saisies par l'utilisateur en cas d'erreur
     *
     * @param id    id du dossier
     * @param model model de la vue dossier/view
     * @return la vue dossier/view
     */
    @GetMapping(value = "/dossier/{id}")
    public Mono<String> getClientByIdAsync(@PathVariable String id, Model model, Authentication authentication) {
        String accessToken = tokenResolver.resolveAccessToken(authentication);
        return webClient.get()
                        .uri("/queries/dossiers/" + id)
                .headers(headers -> {
                    if (StringUtils.hasText(accessToken)) {
                        headers.setBearerAuth(accessToken);
                    }
                })
                        .retrieve()
                        .bodyToMono(DossierThymConvertDTO.class)
                        .onErrorResume(throwable -> Mono.error(new RuntimeException("Erreur lors de la récupération du dossier")))
                        .flatMap(dto -> {
                            assert dto != null;
                            model.addAttribute("dossier", dto);
                            return Mono.just("dossier/view");
                        });
    }
    
    /**
     * Affiche la liste de tous les dossiers enregistrés
     *
     * @param model              model de la vue dossier/dossiers
     * @return la vue dossier/dossiers
     */
    @GetMapping(value = "/dossiers")
    public Mono<String> getDossiersAsync(Model model, Authentication authentication) {
        String accessToken = tokenResolver.resolveAccessToken(authentication);
        return webClient.get()
                        .uri("/queries/dossiers")
                        .headers(headers -> {
                            if (StringUtils.hasText(accessToken)) {
                                headers.setBearerAuth(accessToken);
                            }
                        })
                        .retrieve()
                        .bodyToFlux(DossierThymDTO.class)
                        .collectList()
                        .flatMap(dossiers -> {
                            assert dossiers != null;
                            model.addAttribute("dossiers", dossiers);
                            return Mono.just("dossier/dossiers");
                        })
                        .onErrorResume(WebClientResponseException.class, e -> {
                            FrontendLoggers.error().error("UI_DOSSIER_LIST_FAILED status={} message={}", e.getStatusCode(), e.getResponseBodyAsString());
                            model.addAttribute("alertClass", "alert-danger");
                            model.addAttribute("urlRedirection", "/dossiers");
                            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                                model.addAttribute("errorMessage", "Accès interdit : vous n'avez pas les droits pour consulter les dossiers.");
                            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                                model.addAttribute("errorMessage", "Non authentifié : veuillez vous reconnecter.");
                            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                                model.addAttribute("errorMessage", "Aucun dossier trouvé.");
                            } else {
                                model.addAttribute("errorMessage", "Erreur inattendue (" + e.getStatusCode() + ").");
                            }
                            return Mono.just("error");
                        })
                        .onErrorResume(Exception.class, e -> {
                            FrontendLoggers.error().error("UI_DOSSIER_LIST_UNEXPECTED_ERROR message={}", e.getMessage(), e);
                            model.addAttribute("alertClass", "alert-danger");
                            model.addAttribute("urlRedirection", "/dossiers");
                            model.addAttribute("errorMessage", "Erreur de connexion au serveur : " + e.getMessage());
                            return Mono.just("error");
                        });
    }
}
