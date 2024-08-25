package fr.cdrochon.smamonolithe.vehicule.query.mapper;

import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import org.springframework.stereotype.Component;

@Component
public class VehiculeQueryMapper {
    
    /**
     * Convertit une entité vehicule en VehiculeQueryDTO
     *
     * @param vehicule Vehicule à convertir
     * @return VehiculeQueryDTO
     */
    public static VehiculeQueryDTO convertVehiculeToVehiculeDTO(Vehicule vehicule) {
        if(vehicule == null) {
            return null;
        }
        VehiculeQueryDTO dto = new VehiculeQueryDTO();
        dto.setId(vehicule.getId());
        dto.setImmatriculationVehicule(vehicule.getImmatriculationVehicule());
        dto.setDateMiseEnCirculationVehicule(vehicule.getDateMiseEnCirculationVehicule());
        dto.setVehiculeStatus(vehicule.getVehiculeStatus());
        
        return dto;
    }
}
