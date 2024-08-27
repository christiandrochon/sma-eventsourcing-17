package fr.cdrochon.smamonolithe.vehicule.query.mapper;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;

import java.util.HashMap;
import java.util.Map;

import static fr.cdrochon.smamonolithe.client.query.mapper.AdresseQueryMapper.convertAdresseToClientAdresseDTO;
import static fr.cdrochon.smamonolithe.client.query.mapper.ClientQueryMapper.convertClientToClientDTO;
import static fr.cdrochon.smamonolithe.vehicule.query.mapper.VehiculeQueryMapper.convertVehiculeToVehiculeDTO;

public class RecursiveConversionClientVehicule {
    
    private static final Map<String, ClientQueryDTO> clientMap = new HashMap<>();
    private static final Map<String, VehiculeQueryDTO> vehiculeMap = new HashMap<>();
    
    public static ClientQueryDTO addClientQueryMapper(Client client) {
        if(clientMap.containsKey(client.getId())) {
            return clientMap.get(client.getId());
        }
        ClientQueryDTO clientQueryDTO = new ClientQueryDTO();
        //control de l'appel recursif des methodes elles-memes grace à la map
        clientMap.put(client.getId(), clientQueryDTO);
        
        clientQueryDTO.setId(client.getId());
        clientQueryDTO.setNomClient(client.getNomClient());
        clientQueryDTO.setPrenomClient(client.getPrenomClient());
        clientQueryDTO.setMailClient(client.getMailClient());
        clientQueryDTO.setTelClient(client.getTelClient());
        //PAS BESOIN DE CONVERTIR UN ENUM EN DTO
        clientQueryDTO.setClientStatus(client.getClientStatus());
        
        ClientAdresseDTO clientAdresseDTO = convertAdresseToClientAdresseDTO(client.getAdresse());
        clientQueryDTO.setAdresse(clientAdresseDTO);
        
        // control de l'appel recursif de l'objet vehicule
        if(client.getVehicule() != null && !vehiculeMap.containsKey(client.getVehicule().getId())) {
            clientQueryDTO.setVehicule(convertVehiculeToVehiculeDTO(client.getVehicule()));
        }
        
        return clientQueryDTO;
    }
    
    public static VehiculeQueryDTO addVehiculeQueryMapper(Vehicule vehicule) {
        if(vehiculeMap.containsKey(vehicule.getId())) {
            return vehiculeMap.get(vehicule.getId());
        }
        VehiculeQueryDTO vehiculeQueryDTO = new VehiculeQueryDTO();
        //control de l'appel recursif de la methode grace à la map
        vehiculeMap.put(vehicule.getId(), vehiculeQueryDTO);
        
        vehiculeQueryDTO.setId(vehicule.getId());
        vehiculeQueryDTO.setImmatriculationVehicule(vehicule.getImmatriculationVehicule());
        vehiculeQueryDTO.setDateMiseEnCirculationVehicule(vehicule.getDateMiseEnCirculationVehicule());
        vehiculeQueryDTO.setVehiculeStatus(vehicule.getVehiculeStatus());
        
        // control de l'appel recursif de l'objet client
        if(vehicule.getClient() != null && !clientMap.containsKey(vehicule.getClient().getId())) {
            vehiculeQueryDTO.setClient(convertClientToClientDTO(vehicule.getClient()));
        }
        
        return vehiculeQueryDTO;
    }
}
