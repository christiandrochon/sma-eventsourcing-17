package fr.cdrochon.smamonolithe.vehicule.query.mapper;

import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import org.springframework.stereotype.Component;

import static fr.cdrochon.smamonolithe.vehicule.query.mapper.RecursiveConversionClientVehicule.addVehiculeQueryMapper;


@Component
public class VehiculeQueryMapper {
    
    /**
     * Convertit une entité vehicule en VehiculeQueryDTO. Pour eviter les appels recursifs, on etends la classes RecursiveConversionClientVehicule
     *
     * @param vehicule Vehicule à convertir
     * @return VehiculeQueryDTO converti
     */
    public static VehiculeQueryDTO convertVehiculeToVehiculeDTO(Vehicule vehicule) {
        return addVehiculeQueryMapper(vehicule);
    }
}
