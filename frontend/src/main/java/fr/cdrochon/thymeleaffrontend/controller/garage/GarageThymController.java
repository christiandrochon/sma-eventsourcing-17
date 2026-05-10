package fr.cdrochon.thymeleaffrontend.controller.garage;
import fr.cdrochon.thymeleaffrontend.dtos.garage.GarageGetDTO;
import fr.cdrochon.thymeleaffrontend.dtos.garage.GaragePostDTO;
import fr.cdrochon.thymeleaffrontend.security.FrontendTokenResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
@Controller
public class GarageThymController {
    @Autowired
    private WebClient webClient;
    @Autowired
    private FrontendTokenResolver tokenResolver;
    @GetMapping(value = "/garage/{id}")
    public Mono<String> getGarageByIdAsync(@PathVariable String id, Model model, Authentication authentication) {
        String accessToken = tokenResolver.resolveAccessToken(authentication);
        return webClient.get()
                        .uri("/queries/garages/" + id)
                        .headers(h -> { if (accessToken != null && !accessToken.isBlank()) h.setBearerAuth(accessToken); })
                        .retrieve()
                        .bodyToMono(GaragePostDTO.class)
                        .onErrorResume(t -> Mono.error(new RuntimeException("Erreur lors de la récupération du garage")))
                        .flatMap(dto -> {
                            model.addAttribute("garage", dto);
                            return Mono.just("garage/view");
                        });
    }
    @GetMapping("/garages")
    public Mono<String> getGaragesAsyncClient(Model model, Authentication authentication) {
        String accessToken = tokenResolver.resolveAccessToken(authentication);
        return webClient.get()
                        .uri("/queries/garages")
                        .headers(h -> { if (accessToken != null && !accessToken.isBlank()) h.setBearerAuth(accessToken); })
                        .retrieve()
                        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                                  r -> Mono.error(new RuntimeException("Erreur lors de la récupération des garages")))
                        .bodyToFlux(GarageGetDTO.class)
                        .collectList()
                        .flatMap(garages -> {
                            model.addAttribute("garages", garages);
                            return Mono.just("garage/garages");
                        });
    }
}
