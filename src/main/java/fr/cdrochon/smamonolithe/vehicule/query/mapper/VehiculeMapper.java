package fr.cdrochon.smamonolithe.vehicule.query.mapper;

import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import org.springframework.stereotype.Component;

@Component
public class VehiculeMapper {
    
    public static VehiculeQueryDTO convertVehiculeToVehiculeDTO(Vehicule vehicule) {
        if(vehicule == null) {
            return null;
        }
        VehiculeQueryDTO dto = new VehiculeQueryDTO();
        dto.setIdVehicule(vehicule.getIdVehicule());
        dto.setImmatriculationVehicule(vehicule.getImmatriculationVehicule());
        dto.setDateMiseEnCirculationVehicule(vehicule.getDateMiseEnCirculationVehicule());
        dto.setVehiculeStatus(vehicule.getVehiculeStatus());
        
        return dto;
    }
}
