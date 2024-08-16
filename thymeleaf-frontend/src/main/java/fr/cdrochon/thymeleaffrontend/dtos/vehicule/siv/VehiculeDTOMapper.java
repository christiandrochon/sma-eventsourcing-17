package fr.cdrochon.thymeleaffrontend.dtos.vehicule.siv;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class VehiculeDTOMapper {
    
    /**
     * Convertit un VehiculeSIVConvertDTO en VehiculeSIVDTO
     *
     * @param vehiculeSIVConvertDTO VehiculeSIVConvertDTO
     * @return VehiculeSIVDTO
     */
    public static VehiculeSIVDTO toVehiculeSIVDTO(VehiculeSIVConvertDTO vehiculeSIVConvertDTO) {
        return VehiculeSIVDTO.builder()
                             .immatriculation(vehiculeSIVConvertDTO.getImmatriculation())
                             .dateDeMiseEnCirculation(vehiculeSIVConvertDTO.getDateDeMiseEnCirculation().toString())
                             .dateValiditeControleTechnique(vehiculeSIVConvertDTO.getDateValiditeControleTechnique().toString())
                             .modele(vehiculeSIVConvertDTO.getModele())
                             .marque(vehiculeSIVConvertDTO.getMarque().toString())
                             .typeCarburant(vehiculeSIVConvertDTO.getTypeCarburant().getTypeCarburant().toString())
                             .climatisation(vehiculeSIVConvertDTO.isClimatisation())
                             .build();
    }
    
    /**
     * Convertit un VehiculeSIVDTO en VehiculeSIVConvertDTO
     *
     * @param vehiculeSIVDTO VehiculeSIVDTO
     * @return VehiculeSIVConvertDTO
     */
    public static VehiculeSIVConvertDTO toVehiculeSIVConvertDTO(VehiculeSIVDTO vehiculeSIVDTO) {
        
        return VehiculeSIVConvertDTO.builder()
                                    .immatriculation(vehiculeSIVDTO.getImmatriculation())
                                    .dateDeMiseEnCirculation(Instant.parse(vehiculeSIVDTO.getDateDeMiseEnCirculation() + "T00:00:00Z"))
                                    .dateValiditeControleTechnique(Instant.parse(vehiculeSIVDTO.getDateValiditeControleTechnique() + "T00:00:00Z"))
                                    .modele(vehiculeSIVDTO.getModele())
                                    .marque(MarqueVehiculeDTO.builder().marques(List.of(vehiculeSIVDTO.getMarque().toUpperCase())).build())
                                    .typeCarburant(TypeCarburantDTO.builder()
                                                                   .typeCarburant(List.of(vehiculeSIVDTO.getTypeCarburant().toUpperCase()))
                                                                   .build())
                                    .climatisation(vehiculeSIVDTO.isClimatisation())
                                    .build();
    }
}
