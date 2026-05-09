package fr.cdrochon.smamonolithe.client.command.dtos;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;
import org.springframework.stereotype.Component;

@Component
public class AdresseCommandMapper {
    
    /**
     * Convertit le DTO d'une adresse d'un client en entité AdresseClient
     *
     * @param clientAdresseDTO DTO d'une adresse d'un client
     * @return entité AdresseClient
     */
    public static AdresseClient convertAdresseCommandDTOToAdresse(ClientAdresseDTO clientAdresseDTO) {
        return AdresseClient.builder()
                            .rue(clientAdresseDTO.getRue())
                            .numeroDeRue(clientAdresseDTO.getNumeroDeRue())
                            .complementAdresse(clientAdresseDTO.getComplementAdresse())
                            .cp(clientAdresseDTO.getCp())
                            .ville(clientAdresseDTO.getVille())
                            .pays(clientAdresseDTO.getPays())
                            .build();
    }
    
    /**
     * Convertit une entité AdresseClient en DTO d'une adresse d'un client
     *
     * @param adresseClient entité AdresseClient
     * @return DTO d'une adresse d'un client
     */
    public static ClientAdresseDTO convertAdresseToClientAdresseDTO(AdresseClient adresseClient) {
        return ClientAdresseDTO.builder()
                               .rue(adresseClient.getRue())
                               .numeroDeRue(adresseClient.getNumeroDeRue())
                               .complementAdresse(adresseClient.getComplementAdresse())
                               .cp(adresseClient.getCp())
                               .ville(adresseClient.getVille())
                               .pays(adresseClient.getPays())
                               .build();
    }
    
}
