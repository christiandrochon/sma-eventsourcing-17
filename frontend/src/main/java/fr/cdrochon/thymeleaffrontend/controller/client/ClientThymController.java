package fr.cdrochon.thymeleaffrontend.controller.client;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymConvertDTO;
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
public class ClientThymController {
    
    @Autowired
    private WebClient webClient;
    
    /**
     * Affiche les détails d'un client spécifique
     *
     * @param id    id du client
     * @param model model de la vue client/view
     * @return la vue client/view
     */
     @GetMapping(value = "/client/{id}")
     public Mono<String> getClientByIdAsync(@PathVariable String id, Model model) {
         FrontendLoggers.business().info("UI_CLIENT_QUERY clientId={}", id);
         return webClient.get()
                         .uri("/queries/clients/" + id)
                         //                                  .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " +
                         //                                  getJwtTokenValue()))
                         .retrieve()
                         .bodyToMono(ClientThymConvertDTO.class)
                         .onErrorResume(throwable -> Mono.error(new RuntimeException("Erreur lors de la récupération du client")))
                         .flatMap(clientThymDTO -> {
                             FrontendLoggers.access().info("UI_CLIENT_RETRIEVED clientId={} nomClient={}", id, clientThymDTO.getNomClient());
                             model.addAttribute("client", clientThymDTO);
                             return Mono.just("client/view");
                         });
     }

    /**
     * Affiche la liste des clients
     *
     * @param model              model de la vue client/clients
     * @param redirectAttributes attributs de redirection
     * @return la vue client/clients
     */
     @GetMapping(value = "/clients")
     public Mono<String> getClientsAsync(Model model) {
         FrontendLoggers.business().info("UI_CLIENTS_LIST_REQUEST");
         return webClient.get()
                         .uri("/queries/clients")
                         .retrieve()
                         .bodyToFlux(ClientThymConvertDTO.class)
                         .collectList()
                         .flatMap(clients -> {
                             assert clients != null;
                             FrontendLoggers.business().info("UI_CLIENTS_LIST_RETRIEVED count={}", clients.size());
                             clients.forEach(client -> client.setTelClient(formaterNumeroTelephone(client.getTelClient())));
                             model.addAttribute("clients", clients);
                             return Mono.just("client/clients");
                         })
                        .onErrorResume(WebClientResponseException.class, e -> {
                            FrontendLoggers.error().error("UI_CLIENT_LIST_FAILED status={} message={}", e.getStatusCode(), e.getResponseBodyAsString());
                            model.addAttribute("alertClass", "alert-danger");
                            model.addAttribute("urlRedirection", "/dossiers");
                            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                                model.addAttribute("errorMessage", "Accès interdit : vous n'avez pas les droits pour consulter la liste des clients.");
                            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                                model.addAttribute("errorMessage", "Non authentifié : veuillez vous reconnecter.");
                            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                                model.addAttribute("errorMessage", "Aucun client trouvé.");
                            } else if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                                model.addAttribute("errorMessage", "Erreur interne du serveur. Réessayez plus tard.");
                            } else {
                                model.addAttribute("errorMessage", "Erreur inattendue (" + e.getStatusCode() + ").");
                            }
                            return Mono.just("error");
                        })
                        .onErrorResume(Exception.class, e -> {
                            FrontendLoggers.error().error("UI_CLIENT_LIST_UNEXPECTED_ERROR message={}", e.getMessage(), e);
                            model.addAttribute("alertClass", "alert-danger");
                            model.addAttribute("urlRedirection", "/dossiers");
                            model.addAttribute("errorMessage", "Erreur de connexion au serveur : " + e.getMessage());
                            return Mono.just("error");
                        });
     }

    /**
     * >Formatage du numéro de téléphone, avec un formatage de type "XX XX XX XX XX"
     *
     * @param numero le numéro de téléphone à formater
     * @return le numéro de téléphone formaté
     */
    public String formaterNumeroTelephone(String numero) {
        if(numero == null) {
            return "";
        }
        return numero.replaceAll("(\\d{2})(?=(\\d{2})+(?!\\d))", "$1 ");
    }
}
