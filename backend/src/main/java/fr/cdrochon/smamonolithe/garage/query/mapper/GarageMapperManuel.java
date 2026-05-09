package fr.cdrochon.smamonolithe.garage.query.mapper;

import fr.cdrochon.smamonolithe.garage.query.dto.GarageAdresseDTO;
import fr.cdrochon.smamonolithe.garage.query.dto.GarageQueryDTO;
import fr.cdrochon.smamonolithe.garage.query.entities.Garage;
import org.springframework.stereotype.Component;

@Component
public class GarageMapperManuel {
   
    public static GarageQueryDTO convertGarageToGarageDTO(Garage garage){
        if(garage == null){
            return null;
        }
        
        GarageQueryDTO dto = new GarageQueryDTO();
        dto.setId(garage.getIdQuery());
        dto.setNomGarage(garage.getNomGarage());
        dto.setMailResp(garage.getMailResponsable());
        
        GarageAdresseDTO adresseDTO = new GarageAdresseDTO();
        adresseDTO.setNumeroDeRue(garage.getAdresseGarage().getNumeroDeRue());
        adresseDTO.setRue(garage.getAdresseGarage().getRue());
        adresseDTO.setCp(garage.getAdresseGarage().getCp());
        adresseDTO.setVille(garage.getAdresseGarage().getVille());
        
        dto.setAdresse(adresseDTO);
        
        return dto;
    }
}
