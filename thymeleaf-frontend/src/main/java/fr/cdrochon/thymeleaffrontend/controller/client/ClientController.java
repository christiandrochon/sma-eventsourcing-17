package fr.cdrochon.thymeleaffrontend.controller.client;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientPostDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;

import java.util.List;

@Controller
public class ClientController {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    
    private final RestClient restClient;
    
    public ClientController(RestClient restClient) {
        this.restClient = restClient;
    }
    
    @GetMapping("/client/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public String clientById(@PathVariable String id, Model model) {
        ClientPostDTO client = restClient.get().uri(externalServiceUrl + "/queries/clients/" + id)
                                         //                                  .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " +
                                         //                                  getJwtTokenValue()))
                                         .retrieve().body(new ParameterizedTypeReference<>() {
                });
        model.addAttribute("client", client);
        return "client/view";
    }
    
    @GetMapping("/clients")
    //    @PreAuthorize("hasAuthority('USER')")
    public String clients(Model model) {
        List<ClientPostDTO> clients =
                restClient.get()
                          .uri(externalServiceUrl + "/queries/clients")
                          //                          .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                          .retrieve().body(new ParameterizedTypeReference<>() {
                          });
        
        assert clients != null;
        clients.forEach(client -> client.setTelClient(formaterNumeroTelephone(client.getTelClient())));
        model.addAttribute("clients", clients);
        return "client/clients";
    }
    
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
