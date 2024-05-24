package fr.cdrochon.smamonolithe.client.controller;

import fr.cdrochon.smamonolithe.client.entity.Client;
import fr.cdrochon.smamonolithe.client.repository.ClientRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClientRestController {
    
    private final ClientRepository clientRepository;
    
    public ClientRestController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }
    
    @GetMapping("/client/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public Client getClientById(@PathVariable Long id) {
        return clientRepository.findById(id)
                               .get();
    }
    
    @GetMapping("/clients")
    @PreAuthorize("hasAuthority('USER')")
    public List<Client> getClients() {
        return clientRepository.findAll();
    }
}
