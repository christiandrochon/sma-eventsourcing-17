package fr.cdrochon.smamonolithe.client.query.mapper;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.entities.AdresseClient;

public class AdresseMapper {

    public static ClientAdresseDTO convertAdresseToClientAdresseDTO(AdresseClient adresseClient) {
        if(adresseClient == null) {
            return null;
        }

        ClientAdresseDTO dto = new ClientAdresseDTO();
        dto.setNumeroDeRue(adresseClient.getNumeroDeRue());
        dto.setRue(adresseClient.getRue());
        dto.setCp(adresseClient.getCp());
        dto.setVille(adresseClient.getVille());
        dto.setPays(adresseClient.getPays());

        return dto;
    }
}
