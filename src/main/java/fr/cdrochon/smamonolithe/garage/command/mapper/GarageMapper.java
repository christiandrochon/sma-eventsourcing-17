package fr.cdrochon.smamonolithe.garage.command.mapper;

import fr.cdrochon.smamonolithe.garage.query.dto.GarageResponseDTO;
import fr.cdrochon.smamonolithe.garage.query.entities.Garage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GarageMapper {
    
    @Mapping(source = "idQuery", target = "id")
    @Mapping(source = "nomGarage", target = "nomGarage")
    @Mapping(source = "mailResponsable", target = "mailResp")
//    @Mapping(source = "numeroDeRue", target = "numeroDeRue")
        //    @Mapping(source = "adresse", target = "adresseGarage")
        //    @Mapping(source = "mailResponsable", target = "emailContactGarage")
    GarageResponseDTO garageQueryToGarageQueryDTO(Garage garage);
}
