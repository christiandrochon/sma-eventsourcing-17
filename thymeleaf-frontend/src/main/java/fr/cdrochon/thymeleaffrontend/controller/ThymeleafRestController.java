package fr.cdrochon.thymeleaffrontend.controller;

import fr.cdrochon.thymeleaffrontend.dtos.GaragePostDTO;
import fr.cdrochon.thymeleaffrontend.entity.Client;
import fr.cdrochon.thymeleaffrontend.entity.Document;
import fr.cdrochon.thymeleaffrontend.entity.Garage;
import fr.cdrochon.thymeleaffrontend.entity.Vehicule;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.security.oauth2.client.registration.ClientRegistration;
//import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
//import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class ThymeleafRestController {
//    private ClientRegistrationRepository clientRegistrationRepository;
    
    RestClient restClient = RestClient.create("http://localhost:8092");
    
//    public ThymeleafRestController(ClientRegistrationRepository clientRegistrationRepository) {
//        this.clientRegistrationRepository = clientRegistrationRepository;
//    }
    
    @GetMapping("/create")
//    @PreAuthorize("hasAuthority('ADMIN')")
    public String createGarage(Model model) {
        model.addAttribute("garageDTO", new GaragePostDTO());
        return "createGarageForm";
    }
    
    @PostMapping("/create")
//    @PreAuthorize("hasAuthority('ADMIN')")
    public ModelAndView createGarage(@ModelAttribute GaragePostDTO garageDTO) {
        try {
            Garage garage = new Garage();
//            garage.setId(garageDTO.getId());
            garage.setNomGarage(garageDTO.getNomGarage());
            garage.setEmailContactGarage(garageDTO.getMailResp());
            garage.setAdresseGarage(garageDTO.getAdresse());
            
//            MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(garage);
            //            mappingJacksonValue.setSerializationView(gara);
            
//           ResponseEntity<Void> responseEntity =
                   restClient.post().uri("/commands/create")
//                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
//                      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                   .contentType(MediaType.APPLICATION_JSON)
                      .body(garage).retrieve().toBodilessEntity();
                   
//            System.out.println(responseEntity);
            return new ModelAndView("redirect:/garages");
        } catch(Exception e) {
            System.out.println("ERRRRRRRRRRRRRRRRROOR : " + e.getMessage());
            return new ModelAndView("createGarageForm");
        }
    }
    
    //    @PostMapping("/create")
    //    public ModelAndView createGarage(@Valid @ModelAttribute("createGarageForm") CreateGarageForm garageForm,
    //                                     BindingResult bindingResult,
    //                                     RedirectAttributes redirectAttributes) {
    //        if(bindingResult.hasErrors()) {
    //            return new ModelAndView(GARAGE_CREATE_FORM);
    //        }
    //        else {
    //            String newId;
    //            try {
    //                Garage garage = new Garage();
    //                garage.setId(garageForm.getId());
    //                garage.setNomGarage(garageForm.getNomGarage());
    //                garage.setEmailContactGarage(garageForm.getMailResponsable());
    //                garageRepository.save(garage);
    //
    //                //newId = save()
    //                redirectAttributes.addFlashAttribute("successMessage", "Garage is created !");
    //                return new ModelAndView("redirect:/");
    //
    //            } catch(Exception e) {
    //                bindingResult.reject(ErrorCodes.EMPTY_MODEL_STACK);
    //                return new ModelAndView(GARAGE_CREATE_FORM);
    //
    //            }
    //        }
    //    }
    
//    @GetMapping("/garages/{id}")
    
//    /**
//     * Affiche les données d'un garage
//     * @param id
//     * @param model
//     * @return
//     */
//    @GetMapping(value = "/garage/{id}")
////    @PreAuthorize("hasAuthority('USER')")
//    public String garageById(@PathVariable String id, Model model) {
//        GaragePostDTO garage = restClient.get().uri("/queries/garages/" + id)
////                                  .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
//                                  .retrieve().body(new ParameterizedTypeReference<GaragePostDTO>() {});
////        System.out.println("resultat de la requete : " + garage.getNomGarage());
//        model.addAttribute("garage", garage);
//        return "garage/view";
//    }
//
//    /**
//     * Requete vers le ms garage avec RestClient
//     *
//     * @param model
//     * @return
//     */
//    @GetMapping("/garages")
////    @PreAuthorize("hasAuthority('USER')")
//    public String garages(Model model) {
//        List<Garage> garages = restClient.get().uri("/queries/garages")
////                                         .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
//                                         .retrieve().body(new ParameterizedTypeReference<List<Garage>>() {
//                });
//        model.addAttribute("garages", garages);
//        return "garages";
//    }
    
    
    @GetMapping("/client/{id}")
//    @PreAuthorize("hasAuthority('USER')")
    public String clientById(@PathVariable Long id, Model model) {
        Client client = restClient.get().uri("/clients/" + id)
//                                  .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                                  .retrieve().body(new ParameterizedTypeReference<>() {
                });
        model.addAttribute("clients", client);
        return "clients";
    }
    
    @GetMapping("/clients")
//    @PreAuthorize("hasAuthority('USER')")
    public String clients(Model model) {
        List<Client> clients =
                restClient.get()
                          .uri("/clients")
//                          .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                          .retrieve().body(new ParameterizedTypeReference<>() {
                          });
        model.addAttribute("clients", clients);
        return "clients";
    }
    
    
    @GetMapping("/vehicule/{id}")
//    @PreAuthorize("hasAuthority('USER')")
    public String vehiculeById(@PathVariable Long id, Model model) {
        Vehicule vehicule = restClient.get().uri("/vehicules/" + id)
//                                      .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                                      .retrieve().body(new ParameterizedTypeReference<Vehicule>() {
                });
        model.addAttribute("vehicules", vehicule);
        return "vehicules";
    }
    
    @GetMapping("/vehicules")
//    @PreAuthorize("hasAuthority('USER')")
    public String vehicules(Model model) {
        List<Vehicule> vehicules = restClient.get().uri("/vehicules")
//                                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                                             .retrieve().body(new ParameterizedTypeReference<>() {
                });
        model.addAttribute("vehicules", vehicules);
        return "vehicules";
    }
    
    @GetMapping("/document/{id}")
//    @PreAuthorize("hasAuthority('USER')")
    public String documentById(@PathVariable Long id, Model model) {
        Document document = restClient.get().uri("/document/" + id)
//                                      .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                                      .retrieve().body(new ParameterizedTypeReference<>() {
                });
        model.addAttribute("documents", document);
        return "documents";
    }
    
    @GetMapping("/documents")
//    @PreAuthorize("hasAuthority('USER')")
    public String documents(Model model) {
        List<Document> documents = restClient.get().uri("/documents")
//                                             .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
                                             .retrieve().body(new ParameterizedTypeReference<>() {
                });
        model.addAttribute("documents", documents);
        return "documents";
    }
    
    
    /**
     * Path qui permet de recuperer les informations sur la session courante et les users authentifiés
     *
     * @param authentication authentication
     * @return objet authentication au format json grace à @ResponseBody
     */
//    @GetMapping("/auth")
//    @ResponseBody
//    public Authentication authentication(Authentication authentication) {
//        return authentication;
//    }
    
    /**
     * Par defaut, l'appli s'ouvre sans path. Lorsque c'est le cas, on renseigne le path à une page index.html
     *
     * @return
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    /**
     * Renvoi l'user vers la page notAuthorized.html lorsqu'il tente de se rendre sur une url du site dont il n'a pas les droits
     *
     * @return page notAuthorized.html
     */
    @GetMapping("/notAutorized")
    public String notAutorized() {
        return "notAuthorized";
    }
    
    /**
     * Personnalisation de la page d'authentification en affichant la liste des providers, mais avec la possibilité
     * d'ajouter du css ou autre, dont images, etc.
     *
     * @param model
     * @return
     */
//    @GetMapping("/oauth2Login")
//    public String oauth2Login(Model model) {
//        String authorizationRequestBaseUri = "oauth2/authorization";
//        Map<String, String> oauth2AuthenticationUrls = new HashMap();
//        Iterable<ClientRegistration> clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
//        ;
//        clientRegistrations.forEach(registration -> {
//            oauth2AuthenticationUrls.put(registration.getClientName(),
//                                         authorizationRequestBaseUri + "/" + registration.getRegistrationId());
//        });
//        model.addAttribute("urls", oauth2AuthenticationUrls);
//        return "oauth2Login";
//    }
//
    /**
     * Recupere le token jwt de l'user qui s'est authentifié
     * <p>
     * L'objet OAuth2AuthenticationToken suppose qu'on a fait l'authentification avec un provider qui
     * supporte OpenID (keycloak ou google)
     * <p>
     * on doit importer la dependance oauth2-client pour la methode OAuth2AuhtenticationToken
     *
     * @return
     */
//    private String getJwtTokenValue() {
//        SecurityContext context = SecurityContextHolder.getContext();
//        Authentication authentication = context.getAuthentication();
//        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
//        DefaultOidcUser oidcUser = (DefaultOidcUser) oAuth2AuthenticationToken.getPrincipal();
//        String jwtTokenValue = oidcUser.getIdToken()
//                                       .getTokenValue();
//        return jwtTokenValue;
//    }
}
