package fr.cdrochon.smamonolithe.document.command.controllers;

import fr.cdrochon.smamonolithe.document.command.dtos.DocumentRestDTO;
import fr.cdrochon.smamonolithe.document.command.services.DocumentCommandService;
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

@RestController
@RequestMapping("/commands")
public class DocumentCommandController {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final EventStore eventStore;
    private final RestTemplate restTemplate;
    private final DocumentCommandService documentCommandService;
    
    public DocumentCommandController(CommandGateway commandGateway, QueryGateway queryGateway, EventStore eventStore, RestTemplate restTemplate,
                                     DocumentCommandService documentCommandService) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.eventStore = eventStore;
        this.restTemplate = restTemplate;
        this.documentCommandService = documentCommandService;
    }
    
    /**
     * Recoit les informations du dto, et renvoi une commande avec les attributs du dto
     * <p>
     * Les attributs de la command doivent correspondre au dto. Le controller ne fait que retourner le dto et c'est le command handler qui va se charger
     * d'executer cette commande
     * <p>
     * L'id ne peut pas etre negatif
     *
     * @param documentRestDTO DTO contenant les informations du garage a creer
     * @return CompletableFuture<String>
     */
    @GetMapping(value = "/createDocument", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CompletableFuture<String>> createDocumentGet(@RequestBody DocumentRestDTO documentRestDTO) {
        return CompletableFuture.supplyAsync(() -> {
            ResponseEntity<DocumentRestDTO> responseEntity = restTemplate.postForEntity(externalServiceUrl + "/createDocument",
                                                                                          documentRestDTO,
                                                                                          DocumentRestDTO.class);
            DocumentRestDTO responseDto = responseEntity.getBody();
            assert responseDto != null;
            return documentCommandService.createDocument(responseDto);
        });
    }
    
    /**
     * Recoit les informations du dto, et renvoi un une commande avec les attributs du dto
     * <p>
     * Les attributs de la command doivcent correspondre au dto. Le controller ne fait que retourner le dto et c'est le command handler qui va se charger
     * d'executer cette commande
     * <p>
     * L'id ne peut pas etre negatif
     *
     * @param documentDTO DTO contenant les informations du document a creer
     * @return CompletableFuture<String>
     */
    @PostMapping(value = "/createDocument", consumes = MediaType.APPLICATION_JSON_VALUE)
    //    @PreAuthorize("hasRole('USER')")
    //    @PreAuthorize("hasAuthority('USER')")
    public CompletableFuture<String> createDocumentPost(@RequestBody DocumentRestDTO documentDTO) {
        
        try {
            System.out.println(documentDTO.toString());
            String url = "http://localhost:8091/createDocument";
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
                System.out.println(documentDTO);
                
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
                
                return documentCommandService.createDocument(documentDTO);
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
    @GetMapping(path = "/eventStoreDocument/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public Stream readDocumentsInEventStore(@PathVariable String id) {
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
