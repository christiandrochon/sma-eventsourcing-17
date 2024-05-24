package fr.cdrochon.smamonolithe.vehicule.controller;

import fr.cdrochon.smamonolithe.vehicule.entity.Vehicule;
import fr.cdrochon.smamonolithe.vehicule.model.Client;
import fr.cdrochon.smamonolithe.vehicule.repository.VehiculeRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VehiculeRestController {
    
    private VehiculeRepository vehiculeRepository;
    private ClientRestFeign clientRestFeign;

    public VehiculeRestController(VehiculeRepository vehiculeRepository, ClientRestFeign clientRestFeign) {
        this.vehiculeRepository = vehiculeRepository;
        this.clientRestFeign = clientRestFeign;
    }

    @GetMapping("/vehicule/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public Vehicule getVehiculeById(@PathVariable Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id).get();

        Client client = clientRestFeign.findClientById(vehicule.getId());
        vehicule.setClient(client);
        return vehicule;
    }

    @GetMapping("/vehicules")
    @PreAuthorize("hasAuthority('USER')")
    public List<Vehicule> getVehicules() {
        List<Vehicule> vehicules = vehiculeRepository.findAll();

        vehicules.forEach(c -> {
            c.setClient(clientRestFeign.findClientById(c.getClientId()));
        });
        return vehicules;
    }
}
