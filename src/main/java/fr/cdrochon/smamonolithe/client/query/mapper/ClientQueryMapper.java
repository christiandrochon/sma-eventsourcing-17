package fr.cdrochon.smamonolithe.client.query.mapper;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.vehicule.query.mapper.RecursiveConversionClientVehicule;
import org.springframework.stereotype.Component;

@Component
public class ClientQueryMapper extends RecursiveConversionClientVehicule {
    
    /**
     * Convertit une entité client en ClientQueryDTO. Pour eviter les appels recursifs, on etends la classes RecursiveConversionClientVehicule
     *
     * @param client Client à convertir
     * @return ClientQueryDTO converti
     */
    public static ClientQueryDTO convertClientToClientDTO(Client client) {
        return convertClientToClientQueryDTO(client);
    }
}
