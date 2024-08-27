package fr.cdrochon.smamonolithe.vehicule.query.mapper;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;

import java.util.HashMap;
import java.util.Map;

import static fr.cdrochon.smamonolithe.client.query.mapper.AdresseQueryMapper.convertAdresseToClientAdresseDTO;

public class RecursiveConversionClientVehicule {
    
    private static final Map<String, ClientQueryDTO> clientMap = new HashMap<>();
    private static final Map<String, VehiculeQueryDTO> vehiculeMap = new HashMap<>();
    
    /**
     * Convertit le contenu d'une entité client dans un ClientQueryDTO, tout en controllant les appels recursifs de la création d'un vehicule dans un dto
     * client
     *
     * @param client Client à ajouter dans le DTO
     * @return ClientQueryDTO contenant les informations du client
     */
    public static ClientQueryDTO convertClientToClientQueryDTO(Client client) {
        
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
            clientQueryDTO.setVehicule(convertVehiculeToVehiculeQueryDTO(client.getVehicule()));
        }
        
        return clientQueryDTO;
    }
    
    /**
     * Convertit le contenu d'une entité vehicule dans un VehiculeQueryDTO, tout en controllant les appels recursifs de la création d'un client dans ce dto
     * vehicule
     *
     * @param vehicule Vehicule à ajouter dans le DTO
     * @return VehiculeQueryDTO contenant les informations du vehicule
     */
    public static VehiculeQueryDTO convertVehiculeToVehiculeQueryDTO(Vehicule vehicule) {
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
            vehiculeQueryDTO.setClient(convertClientToClientQueryDTO(vehicule.getClient()));
        }
        
        return vehiculeQueryDTO;
    }
}
