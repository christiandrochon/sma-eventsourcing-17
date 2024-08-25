package fr.cdrochon.smamonolithe.client.query.mapper;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import org.springframework.stereotype.Component;

import static fr.cdrochon.smamonolithe.client.query.mapper.AdresseQueryMapper.convertAdresseToClientAdresseDTO;

@Component
public class ClientQueryMapper {
    
    public static ClientQueryDTO convertClientToClientDTO(Client client) {
        if(client == null) {
            return null;
        }
        
        ClientQueryDTO dto = new ClientQueryDTO();
        dto.setId(client.getId());
        dto.setNomClient(client.getNomClient());
        dto.setPrenomClient(client.getPrenomClient());
        dto.setMailClient(client.getMailClient());
        dto.setTelClient(client.getTelClient());
        
        ClientAdresseDTO clientAdresseDTO = convertAdresseToClientAdresseDTO(client.getAdresse());
        
        dto.setAdresse(clientAdresseDTO);
        //PAS BESOIN DE CONVERTIR UN ENUM EN DTO
//        dto.setClientStatus(ClientStatusDTO.valueOf(client.getClientStatus().name()));
        
        return dto;
    }
}
