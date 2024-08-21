package fr.cdrochon.thymeleaffrontend.controller.client;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.garage.GarageGetDTO;
import fr.cdrochon.thymeleaffrontend.dtos.garage.GaragePostDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@Slf4j
public class ClientThymController {
//
//    @Value("${external.service.url}")
//    private String externalServiceUrl;
    
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
    //    @PreAuthorize("hasAuthority('USER')")
    public Mono<String> getClientByIdAsync(@PathVariable String id, Model model) {
        return webClient.get()
                        .uri("/queries/clients/" + id)
                        //                                  .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " +
                        //                                  getJwtTokenValue()))
                        .retrieve()
                        .bodyToMono(ClientThymDTO.class)
                        .onErrorResume(throwable -> Mono.error(new RuntimeException("Erreur lors de la récupération du client")))
                        .flatMap(clientThymDTO -> {
                            model.addAttribute("client", clientThymDTO);
                            return Mono.just("client/view");
                        });
    }
    
    
    //    @GetMapping("/client/{id}")
    //    //    @PreAuthorize("hasAuthority('USER')")
    //    public String clientById(@PathVariable String id, Model model) {
    //        ClientThymDTO client = restClient.get().uri(externalServiceUrl + "/queries/clients/" + id)
    //                                         //                                  .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer
    //                                         " +
    //                                         //                                  getJwtTokenValue()))
    //                                         .retrieve().body(new ParameterizedTypeReference<>() {
    //                });
    //        model.addAttribute("client", client);
    //        return "client/view";
    //    }
    
    /**
     * Affiche la liste des clients
     *
     * @param model              model de la vue client/clients
     * @param redirectAttributes attributs de redirection
     * @return la vue client/clients
     */
    @GetMapping(value = "/clients")
    //    @PreAuthorize("hasAuthority('USER')")
    public Mono<String> getClientsAsync(Model model, RedirectAttributes redirectAttributes) {
        return webClient.get()
                        .uri("/queries/clients")
                        //                                         .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION,
                        //                                         "Bearer " + getJwtTokenValue()))
                        .retrieve()
                        .bodyToFlux(ClientThymDTO.class)
                        .collectList()
                        .flatMap(clients -> {
                            assert clients != null;
                            clients.forEach(client -> client.setTelClient(formaterNumeroTelephone(client.getTelClient())));
                            model.addAttribute("clients", clients);
                            return Mono.just("client/clients");
                        })
                        .onErrorResume(WebClientResponseException.class, e -> {
                            if(e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                                log.error("400 Bad Request: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Requête invalide.");
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createGarage");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.NOT_FOUND) {
                                log.error("404 Not Found: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage", "Ressource non trouvée. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/clients");
                                return Mono.just("redirect:/error");
                            } else if(e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                                log.error("500 Internal Server Error: {}", e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                                redirectAttributes.addFlashAttribute("errorMessage",
                                                                     "Erreur interne de serveur. " + e.getResponseBodyAsString());
                                redirectAttributes.addFlashAttribute("urlRedirection", "/createGarage");
                                return Mono.just("redirect:/error");
                            }
                            log.error("ERREUR: {}", e.getResponseBodyAsString());
                            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                            redirectAttributes.addFlashAttribute("errorMessage", "Erreur non reconnue. " + e.getResponseBodyAsString());
                            redirectAttributes.addFlashAttribute("urlRedirection", "/clients");
                            return Mono.just("redirect:/error");
                        });
        
    }
    
    //    @GetMapping("/clients")
    //    //    @PreAuthorize("hasAuthority('USER')")
    //    public String clients(Model model) {
    //        List<ClientThymDTO> clients =
    //                restClient.get()
    //                          .uri(externalServiceUrl + "/queries/clients")
    //                          //                          .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
    //                          .retrieve().body(new ParameterizedTypeReference<>() {
    //                          });
    //
    //        assert clients != null;
    //        clients.forEach(client -> client.setTelClient(formaterNumeroTelephone(client.getTelClient())));
    //        model.addAttribute("clients", clients);
    //        return "client/clients";
    //    }
    
    /**
     * >Formatage du numéro de téléphone, avec un formatage de type "XX XX XX XX XX"
     *
     * @param numero le numéro de téléphone à formater
     * @return le numéro de téléphone formaté
     */
    public String formaterNumeroTelephone(String numero) {
        return numero.replaceAll("(\\d{2})(?=(\\d{2})+(?!\\d))", "$1 ");
    }
}
