package fr.cdrochon.smamonolithe.garage.command.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.cdrochon.smamonolithe.garage.command.services.GarageCommandService;
import fr.cdrochon.smamonolithe.garage.command.dtos.GarageRestPostDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.springframework.http.HttpHeaders.USER_AGENT;

/**
 * Recoit les requetes de l'exterieur , par ex UI ou autre MS
 * <p>
 * La CommandGateway vient du fw Axon
 */
@RestController
@RequestMapping("/commands")
public class GarageCommandController {
    
    // injection de la command de Axon
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final EventStore eventStore;
    private final GarageCommandService garageQueryCommandService;
    
    //reception de données depuis une interface graphique
//    RestClient restClient = RestClient.create("http://localhost:8091");
    
    public GarageCommandController(CommandGateway commandGateway, QueryGateway queryGateway, EventStore eventStore,
                                   GarageCommandService garageQueryCommandService) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.eventStore = eventStore;
        
        this.garageQueryCommandService = garageQueryCommandService;
    }
    
    
    /**
     * Recoit les informations du dto, et renvoi un une commande avec les attributs du dto
     * <p>
     * Les attributs de la command doivcent correspondre au dto. Le controller ne fait que retourner le dto et c'est le command handler qui va se charger
     * d'executer cette commande
     * <p>
     * L'id ne peut pas etre negatif
     *
     * @param creatClientRequestDTO
     * @return
     */
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
//    @PreAuthorize("hasRole('USER')")
//    @PreAuthorize("hasAuthority('USER')")
    public CompletableFuture<String> createGarage(@RequestBody GarageRestPostDTO creatClientRequestDTO) {
        
        try {
            String url = "http://localhost:8091/create";
            HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
            
            // Optional default is GET
            httpClient.setRequestMethod("GET");
            
            // Add request header
            httpClient.setRequestProperty("User-Agent", USER_AGENT);
//            httpClient.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue());
            
            int responseCode = httpClient.getResponseCode();
            System.out.println("GET Response Code :: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // Print result
                String jsonResponse = response.toString();
                System.out.println(jsonResponse);
                
                // Parse JSON response to Post object
                ObjectMapper objectMapper = new ObjectMapper();
                creatClientRequestDTO = objectMapper.readValue(jsonResponse, GarageRestPostDTO.class);
                
                // Print the post object
                System.out.println(creatClientRequestDTO);
                
                return garageQueryCommandService.createGarage(creatClientRequestDTO);
                
            } else {
                System.out.println("GET request not worked");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        
//        GarageRequestDTO garageRequestDTO = restClient.get().uri("/create/")
//                .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue()))
//                .retrieve().body(new ParameterizedTypeReference<GarageRequestDTO>() {
//                });
        
        
        //        return commandGateway.send(new GarageQueryCreateCommand(UUID.randomUUID().toString(),
        //                                                                creatClientRequestDTO.getNomClient(),
        //                                                                creatClientRequestDTO.getMailResponsable(),
        //                                                                creatClientRequestDTO.getGarageStatus(),
        //                                                                creatClientRequestDTO.getDateQuery()));
        return garageQueryCommandService.createGarage(creatClientRequestDTO);
    }

    
    //    @PostMapping("/create")
    //    public Mono<String> createClient(@RequestBody CreateGarageQueryRequestDTO creatClientRequestDTO) {
    //
    //        return Mono.fromFuture(commandGateway.send(new GarageQueryCreateCommand(UUID.randomUUID().toString(), creatClientRequestDTO.getNomClient(),
    //                                                                                creatClientRequestDTO.getMailResponsable(),
    //                                                                                creatClientRequestDTO.getGarageStatus(), creatClientRequestDTO
    //                                                                                .getDateQuery())));
    //
    //    }
    
    //    @PostMapping("/create")
    //    public Mono<String> createClient(@RequestBody CreateGarageQueryRequestDTO creatClientRequestDTO) {
    //
    //        /* We are wrapping command into GenericCommandMessage, so we can get its identifier (correlation id) */
    //        CommandMessage<Object> command = GenericCommandMessage.asCommandMessage(new GarageQueryCreateCommand(creatClientRequestDTO.getId()));
    //
    //        /* With command identifier we can now subscribe for updates that this command produced */
    //        GarageQueryEventHandlerService query = new GarageQueryEventHandlerService(command.getIdentifier());
    //
    //        /* since we don't care about initial result, we mark it as Void.class */
    //        SubscriptionQueryResult<Void, GarageQuery> response = queryGateway.subscriptionQuery(query,
    //                                                                                             Void.class,
    //                                                                                             GarageQuery.class);
    //        return sendAndReturnUpdate(command, response)
    //                .map(GarageQuery::getIdQuery);
    //
    //    }
    //    public <U> Mono<U> sendAndReturnUpdate(Object command, SubscriptionQueryResult<?, U> result) {
    //        /* The trick here is to subscribe to initial results first, even it does not return any result
    //         Subscribing to initialResult creates a buffer for updates, even that we didn't subscribe for updates yet
    //         they will wait for us in buffer, after this we can safely send command, and then subscribe to updates */
    //        return Mono.when(result.initialResult())
    //                   .then(Mono.fromCompletionStage(() -> commandGateway.send(command)))
    //                   .thenMany(result.updates())
    //                   .timeout(Duration.ofSeconds(5))
    //                   .next()
    //                   .doFinally(unused -> result.cancel());
    //        /* dont forget to close subscription query on the end and add a timeout */
    //    }
    
    
    /**
     * Tester les events du store. On utilise l'id de l'agregat pour consulter l'etat de l'eventstore (json avec tous les events enregistrés)
     * Le format renvoyé est du json dans swagger
     *
     * @param id
     * @return
     */
    @GetMapping(path = "/eventStore/{id}") //consumes = MediaType.TEXT_EVENT_STREAM_VALUE
    @PreAuthorize("hasAuthority('USER')")
    public Stream readEventStore(@PathVariable String id) {
        return eventStore.readEvents(id).asStream();
    }
    
    
    /**
     * Pour recuperer les messages d'erreur lorsqu'une requete s'est mal passée
     *
     * @param exception
     * @return message d'erreur
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
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
    private String getJwtTokenValue() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        DefaultOidcUser oidcUser = (DefaultOidcUser) oAuth2AuthenticationToken.getPrincipal();
        String jwtTokenValue = oidcUser.getIdToken()
                                       .getTokenValue();
        return jwtTokenValue;
    }
}
