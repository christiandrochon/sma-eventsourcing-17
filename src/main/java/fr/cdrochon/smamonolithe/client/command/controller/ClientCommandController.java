package fr.cdrochon.smamonolithe.client.command.controller;

import fr.cdrochon.smamonolithe.client.command.dtos.ClientRestPostDTO;
import fr.cdrochon.smamonolithe.client.command.services.ClientCommandService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@RestController
@RequestMapping("/commands")
public class ClientCommandController {
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final EventStore eventStore;
    private final ClientCommandService clientCommandService;
    
    public ClientCommandController(CommandGateway commandGateway, QueryGateway queryGateway, EventStore eventStore, ClientCommandService clientCommandService) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.eventStore = eventStore;
        this.clientCommandService = clientCommandService;
    }
    
    /**
     * Recoit les informations du dto, et renvoi un une commande avec les attributs du dto
     * <p>
     * Les attributs de la command doivcent correspondre au dto. Le controller ne fait que retourner le dto et c'est le command handler qui va se charger
     * d'executer cette commande
     * <p>
     * L'id ne peut pas etre negatif
     *
     * @param clientRestPostDTO DTO contenant les informations du client a creer
     * @return CompletableFuture<String>
     */
    @PostMapping(value = "/createClient", consumes = MediaType.APPLICATION_JSON_VALUE)
    //    @PreAuthorize("hasRole('USER')")
    //    @PreAuthorize("hasAuthority('USER')")
    public CompletableFuture<String> createClient(@RequestBody ClientRestPostDTO clientRestPostDTO) {
        
        try {
            
            String url = "http://localhost:8091/createClient";
            HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
            int responseCode = httpClient.getResponseCode();
            System.out.println("GET Response Code :: " + responseCode);
            
            if(responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println(clientRestPostDTO.toString());
                
                //            // Ensure the response is not HTML or XML
                //            if(response.toString().trim().startsWith("<")) {
                //                throw new IllegalArgumentException("Expected JSON response but received HTML/XML.");
                //            }
                //
                //            // Print result
                //            String jsonResponse = response.toString();
                //            System.out.println(jsonResponse);
                //
                //            // Parse JSON response to Post object
                //            ObjectMapper objectMapper = new ObjectMapper();
                //            ClientRestPostDTO  clientRequestDTO = objectMapper.readValue(jsonResponse, ClientRestPostDTO.class);
                //
                //            // Print the post object
                //            System.out.println(clientRequestDTO);
                
                return clientCommandService.createClient(clientRestPostDTO);
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
    
    
    //    public CompletableFuture<String> createClient(@RequestBody ClientRestPostDTO clientRequestDTO) {
    //
    //        try {
    //            String url = "http://localhost:8091/createClient";
    //            HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
    //
    //            // Optional default is GET
    //            httpClient.setRequestMethod("GET");
    //
    //            // Add request header
    //            httpClient.setRequestProperty("User-Agent", USER_AGENT);
    //            //            httpClient.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue());
    //
    //            int responseCode = httpClient.getResponseCode();
    //            System.out.println("GET Response Code :: " + responseCode);
    //
    //            if(responseCode == HttpURLConnection.HTTP_OK) { // success
    //                BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()));
    //                String inputLine;
    //                StringBuilder response = new StringBuilder();
    //
    //                while((inputLine = in.readLine()) != null) {
    //                    response.append(inputLine);
    //                }
    //                in.close();
    //
    //                // Print result
    //                String jsonResponse = response.toString();
    //                System.out.println(jsonResponse);
    //
    //                // Parse JSON response to Post object
    //                ObjectMapper objectMapper = new ObjectMapper();
    //                clientRequestDTO = objectMapper.readValue(jsonResponse, ClientRestPostDTO.class);
    //
    //                // Print the post object
    //                System.out.println(clientRequestDTO);
    //
    //                return clientCommandService.createClient(clientRequestDTO);
    //
    //            }
    //            else {
    //                System.out.println("GET request not worked");
    //            }
    //
    //        } catch(Exception e) {
    //            e.printStackTrace();
    //        }
    //        return null;
    //    }
    
    /**
     * Tester les events du store. On utilise l'id de l'agregat pour consulter l'etat de l'eventstore (json avec tous les events enregistrés)
     * Le format renvoyé est du json dans swagger
     *
     * @param id id de l'agregat
     * @return Stream
     */
    @GetMapping(path = "/eventStoreClient/{id}") //consumes = MediaType.TEXT_EVENT_STREAM_VALUE
    //    @PreAuthorize("hasAuthority('USER')")
    public Stream readClientsInEventStore(@PathVariable String id) {
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
}
