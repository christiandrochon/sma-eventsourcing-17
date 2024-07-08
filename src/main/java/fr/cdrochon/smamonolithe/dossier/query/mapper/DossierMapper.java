package fr.cdrochon.smamonolithe.dossier.query.mapper;

import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierResponseDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierStatusDTO;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import org.springframework.stereotype.Component;

@Component
public class DossierMapper {
    
    public static DossierResponseDTO convertDossierToDossierDTO(Dossier dossier){
        DossierResponseDTO dto = new DossierResponseDTO();
        dto.setId(dossier.getId());
        dto.setNomDossier(dossier.getNomDossier());
        dto.setDateCreationDossier(dossier.getDateCreationDossier());
        dto.setDateModificationDossier(dossier.getDateModificationDossier());
        dto.setClient(dossier.getClient());
        dto.setVehicule(dossier.getVehicule());
        dto.setDossierStatus(DossierStatusDTO.valueOf(dossier.getDossierStatus().name()));
        
        return dto;
    }
}
