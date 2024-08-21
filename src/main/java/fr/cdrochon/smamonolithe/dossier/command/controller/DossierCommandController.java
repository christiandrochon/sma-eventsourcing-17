package fr.cdrochon.smamonolithe.dossier.command.controller;

import fr.cdrochon.smamonolithe.dossier.command.dtos.DossierRestDTO;
import fr.cdrochon.smamonolithe.dossier.command.services.DossierCommandService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.springframework.http.HttpHeaders.USER_AGENT;

@RestController
@RequestMapping("/commands")
public class DossierCommandController {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final EventStore eventStore;
    private final DossierCommandService dossierCommandService;
    private final RestTemplate restTemplate;
    public DossierCommandController(CommandGateway commandGateway, QueryGateway queryGateway, EventStore eventStore,
                                    DossierCommandService dossierCommandService,
                                    RestTemplate restTemplate) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.eventStore = eventStore;
        this.dossierCommandService = dossierCommandService;
        this.restTemplate = restTemplate;
    }
    
    /**
     * Sert à ramner les informations sur l'ecran de creation d'un dossier s'il n' pas été validé (en gros, ne pas perdre les données saisies si
     * rafraichissement de l'ecran)
     * <p>
     * Recoit les informations du dto, et renvoi une commande avec les attributs du dto
     * <p>
     * Les attributs de la command doivent correspondre au dto. Le controller ne fait que retourner le dto et c'est le command handler qui va se charger
     * d'executer cette commande
     * <p>
     * L'id ne peut pas etre negatif
     *
     * @param dossierRestDTO DTO contenant les informations du garage a creer
     * @return CompletableFuture<String>
     */
    @GetMapping(value = "/createDossier", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CompletableFuture<String>> createDossierGet(@RequestBody DossierRestDTO dossierRestDTO) {
        return CompletableFuture.supplyAsync(() -> {
            ResponseEntity<DossierRestDTO> responseEntity = restTemplate.postForEntity(externalServiceUrl + "/createDossier",
                                                                                       dossierRestDTO,
                                                                                       DossierRestDTO.class);
            DossierRestDTO responseDto = responseEntity.getBody();
            assert responseDto != null;
            return dossierCommandService.createDossier(responseDto);
        });
    }
    
    /**
     * Recoit les informations du dto, et renvoi un une commande avec les attributs du dto
     * <p>
     * Les attributs de la command doivent correspondre au dto. Le controller ne fait que retourner le dto et c'est le command handler qui va se charger
     * d'executer cette commande
     * <p>
     * L'id ne peut pas etre negatif
     *
     * @param dossierRestDTO DTO contenant les informations du client a creer
     * @return CompletableFuture<String>
     */
    @PostMapping(value = "/createDossier", consumes = MediaType.APPLICATION_JSON_VALUE)
    //    @PreAuthorize("hasRole('USER')")
    //    @PreAuthorize("hasAuthority('USER')")
//    public ResponseEntity<?> createDossier(@RequestBody DossierRestDTO dossierRestDTO) {
//        try {
//            // Ici, vous pouvez ajouter une validation supplémentaire pour dossierRestDTO si nécessaire
//
//            // Appel au service pour créer le dossier et récupérer un identifiant ou un objet résultant
//            String dossierId = dossierCommandService.createDossier(dossierRestDTO).toString();
//
//            // Si tout se passe bien, renvoyer l'ID du dossier avec une réponse 200 OK
//            return ResponseEntity.ok().body("Dossier created successfully with ID: " + dossierId);
//        } catch (Exception e) {
//            // En cas d'erreur, logger l'exception et renvoyer une réponse 500 Internal Server Error
//            e.printStackTrace(); // Considérez utiliser un logger approprié dans une application réelle
//            return new ResponseEntity<>("Failed to create dossier: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
    public CompletableFuture<String> createDocument(@RequestBody DossierRestDTO dossierRestDTO) {

        try {
            String url = externalServiceUrl + "/createDossier";
            HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
            httpClient.setRequestProperty("User-Agent", USER_AGENT);
            //            httpClient.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue());
            httpClient.setRequestMethod("GET");
            
            int responseCode = httpClient.getResponseCode();
            System.out.println("GET Response Code :: " + responseCode);

            if(responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return dossierCommandService.createDossier(dossierRestDTO);
            }
            else {
                System.out.println("GET request not worked, response code: " + responseCode);
                return CompletableFuture.completedFuture("Error: Unexpected response code " + responseCode);
            }
        } catch(Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture("Error: " + e.getMessage());
        }
    }
    
    /**
     * Tester les events du store. On utilise l'id de l'agregat pour consulter l'etat de l'eventstore (json avec tous les events enregistrés)
     * Le format renvoyé est du json dans swagger
     *
     * @param id id de l'agregat
     * @return Stream
     */
    @GetMapping(path = "/eventStoreDossier/{id}") //consumes = MediaType.TEXT_EVENT_STREAM_VALUE
    //    @PreAuthorize("hasAuthority('USER')")
    public Stream readDossiersInEventStore(@PathVariable String id) {
        return eventStore.readEvents(id).asStream();
    }
    
    
    /**
     * Pour recuperer les messages d'erreur lorsqu'une requete s'est mal passée
     *
     * @param exception exception
     * @return message d'erreur
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
