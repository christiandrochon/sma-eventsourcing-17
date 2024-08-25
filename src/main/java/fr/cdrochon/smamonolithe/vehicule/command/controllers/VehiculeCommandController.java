package fr.cdrochon.smamonolithe.vehicule.command.controllers;

import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
import fr.cdrochon.smamonolithe.vehicule.command.services.VehiculeCommandService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpHeaders.USER_AGENT;

@Slf4j
@RestController
@RequestMapping("/commands")
public class VehiculeCommandController {
    
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final EventStore eventStore;
    private final VehiculeCommandService vehiculeCommandService;
    
    private final RestTemplate restTemplate;
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    
    public VehiculeCommandController(CommandGateway commandGateway, QueryGateway queryGateway, EventStore eventStore,
                                     VehiculeCommandService vehiculeCommandService, RestTemplate restTemplate) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.eventStore = eventStore;
        this.vehiculeCommandService = vehiculeCommandService;
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
     * @param vehiculeRequestDTO DTO contenant les informations du vehicule a creer
     * @return CompletableFuture<String>
     */
    @GetMapping(value = "/createVehicule", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CompletableFuture<String>> createGarage(@RequestBody VehiculeCommandDTO vehiculeRequestDTO) {
        
        return CompletableFuture.supplyAsync(() -> {
            ResponseEntity<VehiculeCommandDTO> responseEntity = restTemplate.postForEntity(externalServiceUrl + "/createVehicule",
                                                                                           vehiculeRequestDTO,
                                                                                           VehiculeCommandDTO.class);
            VehiculeCommandDTO responseDto = responseEntity.getBody();
            assert responseDto != null;
            return vehiculeCommandService.createVehicule(responseDto);
        });
    }
    
    
    //todo avec les classes nouvellement crééées
    //    @PostMapping(value = "/createVehicule", consumes = MediaType.APPLICATION_JSON_VALUE)
    //    public CompletableFuture<String> createVehiculePost(@RequestBody VehiculeRestPostDTO vehiculeRestPostDTO) {
    //        return commandGateway.send(new CreateVehiculeCommand(vehiculeRestPostDTO));
    //    }
    
    
    /**
     * Recoit les informations du dto, et renvoi un une commande avec les attributs du dto
     * <p>
     * Les attributs de la command doivcent correspondre au dto. Le controller ne fait que retourner le dto et c'est
     * le command handler qui va se charger
     * d'executer cette commande
     * <p>
     * L'id ne peut pas etre negatif
     *
     * @param vehiculeRestPostDTO DTO contenant les informations du vehicule a creer
     * @return CompletableFuture<String>
     */
    @PostMapping(value = "/createVehicule", consumes = MediaType.APPLICATION_JSON_VALUE)
    //    @PreAuthorize("hasRole('USER')")
    //    @PreAuthorize("hasAuthority('USER')")
    public CompletableFuture<String> createVehicule(@RequestBody VehiculeCommandDTO vehiculeRestPostDTO) {
        System.out.println("createVehicule");
        try {
            String url = externalServiceUrl + "/createVehicule";
            HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
            httpClient.setRequestProperty("User-Agent", USER_AGENT);
            //            httpClient.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtTokenValue());
            
            int responseCode = httpClient.getResponseCode();
            log.info("response code: " + responseCode);
            
            if(responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(httpClient.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                return vehiculeCommandService.createVehicule(vehiculeRestPostDTO);
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
}
