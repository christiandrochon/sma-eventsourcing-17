package fr.cdrochon.smamonolithe.garage.command.mapper;

import fr.cdrochon.smamonolithe.garage.query.dto.GarageAdresseDTO;
import fr.cdrochon.smamonolithe.garage.query.dto.GarageResponseDTO;
import fr.cdrochon.smamonolithe.garage.query.entities.AdresseGarage;
import fr.cdrochon.smamonolithe.garage.query.entities.Garage;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Mapper personnalisé pour recuperer les données @Embedded des GarageDTO
 */
@Component
public class GarageMapperManuel {
   
    public static GarageResponseDTO toGarageDTO(Garage garage){
        
        GarageAdresseDTO garageAdresseDTO = new GarageAdresseDTO();
        garageAdresseDTO.setNumeroDeRue(garage.getAdresseGarage().getNumeroDeRue());
        garageAdresseDTO.setRue(garage.getAdresseGarage().getRue());
        garageAdresseDTO.setCp(garage.getAdresseGarage().getCp());
        garageAdresseDTO.setVille(garage.getAdresseGarage().getVille());
        
        return new GarageResponseDTO(garage.getIdQuery(), garage.getNomGarage(), garage.getMailResponsable(),
                                     garageAdresseDTO, garage.getGarageStatus());
    }
}
