package fr.cdrochon.smamonolithe.client.query.mapper;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientResponseDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientStatusDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {
    
    public static ClientResponseDTO convertClientToClientDTO(Client client) {
        if(client == null) {
            return null;
        }
        
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(client.getId());
        dto.setNomClient(client.getNomClient());
        dto.setPrenomClient(client.getPrenomClient());
        dto.setMailClient(client.getMailClient());
        dto.setTelClient(client.getTelClient());
        
        //        AdresseClient adresseClient = new AdresseClient();
        //        adresseClient.setNumeroDeRue(client.getAdresse().getNumeroDeRue());
        //        adresseClient.setRue(client.getAdresse().getRue());
        //        adresseClient.setCp(client.getAdresse().getCp());
        //        adresseClient.setVille(client.getAdresse().getVille());
        //
        //        dto.setAdresse(adresseClient);
        
        ClientAdresseDTO clientAdresseDTO = new ClientAdresseDTO();
        clientAdresseDTO.setNumeroDeRue(client.getAdresse().getNumeroDeRue());
        clientAdresseDTO.setRue(client.getAdresse().getRue());
        clientAdresseDTO.setCp(client.getAdresse().getCp());
        clientAdresseDTO.setVille(client.getAdresse().getVille());
        
        dto.setAdresse(clientAdresseDTO);
        dto.setClientStatus(ClientStatusDTO.valueOf(client.getClientStatus().name()));
        
        return dto;
    }
}
