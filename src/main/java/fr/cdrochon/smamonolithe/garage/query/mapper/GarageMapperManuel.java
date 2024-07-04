package fr.cdrochon.smamonolithe.garage.query.mapper;

import fr.cdrochon.smamonolithe.garage.query.dto.GarageAdresseDTO;
import fr.cdrochon.smamonolithe.garage.query.dto.GarageResponseDTO;
import fr.cdrochon.smamonolithe.garage.query.entities.Garage;
import org.springframework.stereotype.Component;

/**
 * Mapper personnalisé pour recuperer les données @Embedded des GarageDTO
 */
@Component
public class GarageMapperManuel {
   
    public static GarageResponseDTO convertGarageToGarageDTO(Garage garage){
        GarageResponseDTO dto = new GarageResponseDTO();
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
