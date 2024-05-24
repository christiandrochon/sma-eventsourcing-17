package fr.cdrochon.smamonolithe.vehicule.controller;

import fr.cdrochon.smamonolithe.vehicule.model.AdresseClient;
import fr.cdrochon.smamonolithe.vehicule.model.Client;
import org.springframework.cloud.openfeign.FeignClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Tolerance aux pannes
 */
//@FeignClient(name = "CLIENT-SERVICE", value = "getDefaultAllClients")
@FeignClient(name = "CLIENT-SERVICE", url = "http://localhost:8092")
//@FeignClient(name = "CLIENT-SERVICE")
public interface ClientRestFeign {
    
    @GetMapping("/client/{id}")
    @CircuitBreaker(name = "clientService", fallbackMethod = "getDefaultClient")
        //    @CircuitBreaker(exceptionExpression = "getDefaultClient")
    Client findClientById(@PathVariable Long id);
    
    @GetMapping("/clients")
    //    @CircuitBreaker(exceptionExpression = "getDefaultAllClients")
    @CircuitBreaker(name = "clientService", fallbackMethod = "getDefaultAllClients")
    List<Client> findEveryClients();
    
    /**
     * Comportement par defaut lorsque le microservice client ne repond pas.
     *
     * @param id        id du client
     * @param exception type exception
     * @return objet client vide
     */
    default Client getDefaultClient(Long id,
                                    Exception exception) {
        AdresseClient adresseClient = new AdresseClient("Numero de rue non disponible", "Rue non " +
                "disponible", "CP non disponible", "Ville non disponible");
        
        Client client = new Client();
        client.setId(1L);
        client.setNomClient("Non disponible");
        client.setPrenomClient("Non disponible");
        client.setTelClient("Non disponible");
        client.setMailClient("Non disponible");
        client.setAdresseClient(adresseClient);
        System.err.println("Exception default getDefaultClient : " + exception.getMessage());
        return client;
    }
    
    /**
     * Retourne une liste vide de clients par defaut
     *
     * @param exception type exception
     * @return liste vide de clients
     */
    default List<Client> getDefaultAllClients(Exception exception) {
        
        //        List<Client> clients = new ArrayList<>();
        //        AdresseClient adresseClient = new AdresseClient("nd", "nd", "nd", "nd");
        //        clients.forEach(c-> {
        //            //c.setId(UUID.randomUUID().node());
        //            c.setNomClient("Nom non disponible");
        //            c.setPrenomClient("Prenom non disponible");
        //            c.setMailClient("Mail non disponible");
        //            c.setTelClient("Téléphone non disponible");
        //            c.setAdresseClient(adresseClient);
        //        });
        System.err.println("Exception default getDefaultAllClients : " + exception.getMessage());
        //return clients;
        return List.of();
    }
}
