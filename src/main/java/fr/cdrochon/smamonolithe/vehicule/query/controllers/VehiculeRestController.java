package fr.cdrochon.smamonolithe.vehicule.query.controllers;

import fr.cdrochon.smamonolithe.client.entity.AdresseClient;
import fr.cdrochon.smamonolithe.client.entity.Client;
import fr.cdrochon.smamonolithe.client.repository.ClientRepository;
import fr.cdrochon.smamonolithe.vehicule.entity.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.repository.VehiculeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;

@RestController
public class VehiculeRestController {

    private final VehiculeRepository vehiculeRepository;
    private final ClientRepository clientRepository;

    public VehiculeRestController(VehiculeRepository vehiculeRepository, ClientRepository clientRepository) {
        this.vehiculeRepository = vehiculeRepository;
        this.clientRepository = clientRepository;
    }

    @GetMapping("/vehicule/{id}")
//    @PreAuthorize("hasAuthority('USER')")
    //    @CircuitBreaker(name = "clientService", fallbackMethod = "getDefaultClient")
    public Vehicule getVehiculeById(@PathVariable Long id) {

        Vehicule vehicule = vehiculeRepository.findById(id).get();
        if(vehicule.getClientId() != null) {
            Client client = clientRepository.findById(vehicule.getId()).get();
            vehicule.setClient(client);
        }
        else {
            vehicule.setClient(getDefaultClient());
        }


        return vehicule;
    }

    @GetMapping("/vehicules")
//    @PreAuthorize("hasAuthority('USER')")
    public List<Vehicule> getVehicules() {
        List<Vehicule> vehicules = vehiculeRepository.findAll();

        vehicules.forEach(c -> {
            if(c.getClientId() != null)
                c.setClient(clientRepository.findById(c.getClientId()).get());
            else
                c.setClient(getDefaultClient());
        });
        return vehicules;
    }

    Client getDefaultClient() {
        AdresseClient adresseClient = new AdresseClient("Numero de rue non disponible", "Rue non " +
                "disponible", "CP non disponible", "Ville non disponible");

        Client client = new Client();
        client.setId(new Random().nextLong());
        client.setNomClient("Non disponible");
        client.setPrenomClient("Non disponible");
        client.setTelClient("Non disponible");
        client.setMailClient("Non disponible");
        client.setAdresseClient(adresseClient);
        //System.err.println("Exception default getDefaultClient : " + exception.getMessage());
        return client;
    }
}
