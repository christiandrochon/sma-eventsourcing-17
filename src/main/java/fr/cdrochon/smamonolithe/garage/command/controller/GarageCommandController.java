package fr.cdrochon.smamonolithe.garage.command.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.cdrochon.smamonolithe.garage.command.dtos.GarageRestPostDTO;
import fr.cdrochon.smamonolithe.garage.command.services.GarageCommandService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpHeaders.USER_AGENT;

@RestController
@RequestMapping("/commands")
public class GarageCommandController {
    
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final EventStore eventStore;
    private final GarageCommandService garageQueryCommandService;
    private final RestTemplate restTemplate;
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    
//    RestClient restClient = RestClient.create("http://localhost:8091");
    
    @Autowired
    public GarageCommandController(CommandGateway commandGateway, QueryGateway queryGateway, EventStore eventStore,
                                   GarageCommandService garageQueryCommandService, RestTemplate restTemplate) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.eventStore = eventStore;
        this.garageQueryCommandService = garageQueryCommandService;
        this.restTemplate = restTemplate;
    }
    
    /**
     * Recoit les informations du dto, et renvoi une commande avec les attributs du dto
     * <p>
     * Les attributs de la command doivcent correspondre au dto. Le controller ne fait que retourner le dto et c'est le command handler qui va se charger
     * d'executer cette commande
     * <p>
     * L'id ne peut pas etre negatif
     *
     * @param creatClientRequestDTO
     * @return
     */
//    @PostMapping(value = "/creategarage", consumes = MediaType.APPLICATION_JSON_VALUE)
//    public CompletableFuture<CompletableFuture<String>> createGarage(@RequestBody GarageRestPostDTO creatClientRequestDTO) {
//        return CompletableFuture.supplyAsync(() -> {
//            ResponseEntity<GarageRestPostDTO> responseEntity = restTemplate.postForEntity(externalServiceUrl + "/creategarage",
//                                                                                          creatClientRequestDTO,
//                                                                                          GarageRestPostDTO.class);
//            GarageRestPostDTO responseDto = responseEntity.getBody();
//            assert responseDto != null;
//            return garageQueryCommandService.createGarage(responseDto);
//        });
//    }
    @PostMapping(value = "/createGarage", consumes = MediaType.APPLICATION_JSON_VALUE)
    //    @PreAuthorize("hasRole('USER')")
    //    @PreAuthorize("hasAuthority('USER')")
    public CompletableFuture<String> createGarage(@RequestBody GarageRestPostDTO creatClientRequestDTO) {
        
        try {
            String url = "http://localhost:8091/createGarage";
            HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
            
            // Optional default is GET
            httpClient.setRequestMethod("GET");
            
            // Add request header
            httpClient.setRequestProperty("User-Agent", USER_AGENT);
            //            httpClient.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue());
            
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
                
                // Print result
                String jsonResponse = response.toString();
                System.out.println(jsonResponse);
                
                // Parse JSON response to Post object
                ObjectMapper objectMapper = new ObjectMapper();
                creatClientRequestDTO = objectMapper.readValue(jsonResponse, GarageRestPostDTO.class);
                
                // Print the post object
                System.out.println(creatClientRequestDTO);
                
                return garageQueryCommandService.createGarage(creatClientRequestDTO);
                
            }
            else {
                System.out.println("GET request not worked");
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        return garageQueryCommandService.createGarage(creatClientRequestDTO);
    }
    
}
