package fr.cdrochon.smamonolithe.dossier.command.dtos;

import fr.cdrochon.smamonolithe.client.command.dtos.ClientCommandDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.vehicule.command.dtos.VehiculeCommandDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import org.springframework.stereotype.Component;

import static fr.cdrochon.smamonolithe.client.command.dtos.AdresseCommandMapper.convertAdresseCommandDTOToAdresse;
import static fr.cdrochon.smamonolithe.client.command.dtos.AdresseCommandMapper.convertAdresseToClientAdresseDTO;

@Component
public class DossierCommandMapper {
    
    /**
     * Convertit le dto du véhicule en entité véhicule
     *
     * @param vehiculeCommandDTO dto du véhicule
     * @return entité véhicule
     */
    public static Vehicule convertVehiculeDtoToVehicule(VehiculeCommandDTO vehiculeCommandDTO) {
        if(vehiculeCommandDTO == null) {
            return null;
        }
        
        Vehicule vehicule = new Vehicule();
        vehicule.setIdVehicule(vehiculeCommandDTO.getId());
        vehicule.setImmatriculationVehicule(vehiculeCommandDTO.getImmatriculationVehicule());
        vehicule.setDateMiseEnCirculationVehicule(vehiculeCommandDTO.getDateMiseEnCirculationVehicule());
        vehicule.setVehiculeStatus(vehiculeCommandDTO.getVehiculeStatus());
        
        return vehicule;
    }
    
    /**
     * Convertit le dto du client en entité client
     *
     * @param clientCommandDTO dto du client
     * @return entité client
     */
    public static Client convertClientDtoToClient(ClientCommandDTO clientCommandDTO) {
        if(clientCommandDTO == null) {
            return null;
        }
        
        Client client = new Client();
        client.setId(clientCommandDTO.getId());
        client.setNomClient(clientCommandDTO.getNomClient());
        client.setPrenomClient(clientCommandDTO.getPrenomClient());
        client.setMailClient(clientCommandDTO.getMailClient());
        client.setTelClient(clientCommandDTO.getTelClient());
        client.setAdresse(convertAdresseCommandDTOToAdresse(clientCommandDTO.getAdresse()));
        client.setClientStatus(clientCommandDTO.getClientStatus());
        
        return client;
    }
    
    /**
     * Convertit le client en dto du client
     *
     * @param client client
     * @return dto du client
     */
    public static ClientCommandDTO convertClientToClientDTO(Client client) {
        if(client == null) {
            return null;
        }
        
        ClientCommandDTO clientCommandDTO = new ClientCommandDTO();
        clientCommandDTO.setId(client.getId());
        clientCommandDTO.setNomClient(client.getNomClient());
        clientCommandDTO.setPrenomClient(client.getPrenomClient());
        clientCommandDTO.setMailClient(client.getMailClient());
        clientCommandDTO.setTelClient(client.getTelClient());
        clientCommandDTO.setAdresse(convertAdresseToClientAdresseDTO(client.getAdresse()));
        clientCommandDTO.setClientStatus(client.getClientStatus());
        
        return clientCommandDTO;
    }
    
    /**
     * Convertit le véhicule en dto du véhicule
     *
     * @param vehicule véhicule
     * @return dto du véhicule
     */
    public static VehiculeCommandDTO convertVehiculeToVehiculeDTO(Vehicule vehicule) {
        if(vehicule == null) {
            return null;
        }
        
        VehiculeCommandDTO vehiculeCommandDTO = new VehiculeCommandDTO();
        vehiculeCommandDTO.setId(vehicule.getIdVehicule());
        vehiculeCommandDTO.setImmatriculationVehicule(vehicule.getImmatriculationVehicule());
        vehiculeCommandDTO.setDateMiseEnCirculationVehicule(vehicule.getDateMiseEnCirculationVehicule());
        vehiculeCommandDTO.setVehiculeStatus(vehicule.getVehiculeStatus());
        
        return vehiculeCommandDTO;
    }
}
