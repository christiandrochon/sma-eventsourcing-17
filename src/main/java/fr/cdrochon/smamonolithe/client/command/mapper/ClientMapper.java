package fr.cdrochon.smamonolithe.client.command.mapper;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientResponseDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {
    
    public static ClientResponseDTO convertClientToClientDTO(Client client){
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(client.getId());
        dto.setNomClient(client.getNomClient());
        dto.setPrenomClient(client.getPrenomClient());
        dto.setMailClient(client.getMailClient());
        dto.setTelClient(client.getTelClient());
        
        ClientAdresseDTO clientAdresseDTO = new ClientAdresseDTO();
        clientAdresseDTO.setNumeroDeRue(client.getAdresseClient().getNumeroDeRue());
        clientAdresseDTO.setRue(client.getAdresseClient().getRue());
        clientAdresseDTO.setCp(client.getAdresseClient().getCp());
        clientAdresseDTO.setVille(client.getAdresseClient().getVille());
        
        dto.setAdresse(clientAdresseDTO);
        
        return dto;
    }
}
