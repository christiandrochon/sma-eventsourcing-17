package fr.cdrochon.smamonolithe.vehicule.command.mapper;

import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import org.springframework.stereotype.Component;

@Component
public class VehiculeMapper {
    
    public static Vehicule convertVehiculeToVehiculeDTO(Vehicule vehicule){
        Vehicule dto = new Vehicule();
        dto.setIdVehicule(vehicule.getIdVehicule());
        dto.setImmatriculationVehicule(vehicule.getImmatriculationVehicule());
        dto.setDateMiseEnCirculationVehicule(vehicule.getDateMiseEnCirculationVehicule());
        
        return dto;
    }
}
