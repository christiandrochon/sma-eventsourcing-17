package fr.cdrochon.thymeleaffrontend.controller.garage;

import fr.cdrochon.thymeleaffrontend.dtos.garage.GarageGetDTO;
import fr.cdrochon.thymeleaffrontend.dtos.garage.GaragePostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Controller
public class GarageThymController {
    
    @Autowired
    private WebClient webClient;

    @Autowired(required = false)
    private OAuth2AuthorizedClientService authorizedClientService;

    /**
     * Affiche les données d'un garage
     *
     * @param id    id du garage
     * @param model model de la vue: garage/view
     * @return la vue garage/view
     */
    @GetMapping(value = "/garage/{id}")
    public Mono<String> getGarageByIdAsync(@PathVariable String id, Model model, Authentication authentication) {
        String accessToken = getJwtTokenValue(authentication);
        return webClient.get()
                        .uri("/queries/garages/" + id)
                        .headers(httpHeaders -> {
                            if (accessToken != null && !accessToken.isBlank()) {
                                httpHeaders.setBearerAuth(accessToken);
                            }
                        })
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
    public Mono<String> getGaragesAsyncClient(Model model, Authentication authentication) {
        String accessToken = getJwtTokenValue(authentication);
        return webClient.get()
                        .uri("/queries/garages")
                        .headers(httpHeaders -> {
                            if (accessToken != null && !accessToken.isBlank()) {
                                httpHeaders.setBearerAuth(accessToken);
                            }
                        })
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

    private String getJwtTokenValue(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken) || authorizedClientService == null) {
            return null;
        }
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
        );
        if (client == null || client.getAccessToken() == null) {
            return null;
        }
        return client.getAccessToken().getTokenValue();
    }
}
