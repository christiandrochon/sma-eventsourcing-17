package fr.cdrochon.smamonolithe.client.query.mapper;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import org.springframework.stereotype.Component;

import static fr.cdrochon.smamonolithe.vehicule.query.mapper.RecursiveConversionClientVehicule.addClientQueryMapper;

@Component
public class ClientQueryMapper {
    
    /**
     * Convertit une entité client en ClientQueryDTO. Pour eviter les appels recursifs, on etends la classes RecursiveConversionClientVehicule
     *
     * @param client Client à convertir
     * @return ClientQueryDTO converti
     */
    public static ClientQueryDTO convertClientToClientDTO(Client client) {
        return addClientQueryMapper(client);
    }
}
