package fr.cdrochon.smamonolithe.client.service;

import fr.cdrochon.smamonolithe.client.entity.Client;
import fr.cdrochon.smamonolithe.client.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClientService {
    
    @Autowired
    ClientRepository clientRepository;
    
    public Client findItem(Long itemId) {
        System.out.println("itemId de client service = " + itemId.getClass());

        return clientRepository.getReferenceById(itemId);
    }
}
