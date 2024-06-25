package fr.cdrochon.smamonolithe.event.mapper;

import fr.cdrochon.smamonolithe.event.query.dto.GarageQueryResponseDTO;
import fr.cdrochon.smamonolithe.event.query.entities.GarageQuery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GarageQueryMapper {
    GarageQueryResponseDTO garageQueryToGarageQueryDTO(GarageQuery garageQuery);
}
