package fr.cdrochon.smamonolithe.event.mapper;

import fr.cdrochon.smamonolithe.event.query.dto.GarageQueryResponseDTO;
import fr.cdrochon.smamonolithe.event.query.entities.GarageQuery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GarageQueryMapper {
    
    @Mapping(source = "idQuery", target = "id")
    @Mapping(source = "nomGarage", target = "nomGarage")
    @Mapping(source = "mailResponsable", target = "mailResp")
    GarageQueryResponseDTO garageQueryToGarageQueryDTO(GarageQuery garageQuery);
}
