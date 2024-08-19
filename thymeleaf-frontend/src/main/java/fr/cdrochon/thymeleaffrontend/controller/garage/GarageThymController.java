package fr.cdrochon.thymeleaffrontend.controller.garage;

import fr.cdrochon.thymeleaffrontend.dtos.garage.GarageGetDTO;
import fr.cdrochon.thymeleaffrontend.dtos.garage.GaragePostDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
public class GarageThymController {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    
    @Autowired
    private WebClient webClient;
    
    /**
     * Affiche les données d'un garage
     *
     * @param id    id du garage
     * @param model model de la vue: garage/view
     * @return la vue garage/view
     */
    @GetMapping(value = "/garage/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public Mono<String> getGarageByIdAsync(@PathVariable String id, Model model) {
        return webClient.get()
                        .uri(externalServiceUrl + "/queries/garages/" + id)
                        //                                  .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " +
                        //                                  getJwtTokenValue()))
                        .retrieve()
                        .bodyToMono(GaragePostDTO.class)
                        .onErrorResume(throwable -> Mono.error(new RuntimeException("Erreur lors de la récupération du garage")))
                        .flatMap(garagePostDTO -> {
                            model.addAttribute("garage", garagePostDTO);
                            return Mono.just("garage/view");
                        });
    }
    
    /**
     * Affiche la liste des garages
     *
     * @param model model de la vue: garage/garages
     * @return la vue garage/garages
     */
    @GetMapping("/garages")
    //    @PreAuthorize("hasAuthority('USER')")
    public Mono<String> getGaragesAsyncClient(Model model) {
        return webClient.get()
                        .uri(externalServiceUrl + "/queries/garages")
                        //                                         .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION,
                        //                                         "Bearer " + getJwtTokenValue()))
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                  clientResponse -> Mono.error(new RuntimeException("Erreur lors de la récupération des garages")))
                        .bodyToFlux(GarageGetDTO.class)
                        .collectList()//met tout dans une liste
                        .flatMap(garages -> {
                            //traite la liste
                            model.addAttribute("garages", garages);
                            return Mono.just("garage/garages");
                        });
    }
}
