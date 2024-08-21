package fr.cdrochon.smamonolithe.dossier.query.mapper;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierResponseDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierStatusDTO;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class DossierMapper {
    
    public static VehiculeResponseDTO convertVehiculeToVehiculeDTO(Dossier dossier){
        if(dossier == null){
            return null;
        }
        VehiculeResponseDTO dto = new VehiculeResponseDTO();
        dto.setIdVehicule(dossier.getVehicule().getIdVehicule());
        dto.setImmatriculationVehicule(dossier.getVehicule().getImmatriculationVehicule());
        dto.setDateMiseEnCirculationVehicule(dossier.getVehicule().getDateMiseEnCirculationVehicule());
//        dto.setMarque(dossier.getVehicule().getMarque());
//        dto.setModele(dossier.getVehicule().getModele());
//        dto.setImmatriculation(dossier.getVehicule().getImmatriculation());
//        dto.setAnnee(dossier.getVehicule().getAnnee());
//        dto.setKilometrage(dossier.getVehicule().getKilometrage());
//        dto.setCarburant(dossier.getVehicule().getCarburant());
//        dto.setPuissance(dossier.getVehicule().getPuissance());
//        dto.setCategorie(dossier.getVehicule().getCategorie());
        dto.setVehiculeStatus(dossier.getVehicule().getVehiculeStatus());
        
        return dto;
    }
    
    public static DossierResponseDTO convertDossierToDossierDTO(Dossier dossier){
        if(dossier == null){
            return null;
        }
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
    
    public static ClientQueryDTO convertClientToClientDTO(Dossier dossier){
        if(dossier == null){
            return null;
        }
        ClientQueryDTO dto = new ClientQueryDTO();
        dto.setId(dossier.getClient().getId());
        dto.setNomClient(dossier.getClient().getNomClient());
        dto.setPrenomClient(dossier.getClient().getPrenomClient());
        
        ClientAdresseDTO adresseClientDTO = new ClientAdresseDTO();
        adresseClientDTO.setNumeroDeRue(dossier.getClient().getAdresse().getNumeroDeRue());
        adresseClientDTO.setRue(dossier.getClient().getAdresse().getRue());
        adresseClientDTO.setCp(dossier.getClient().getAdresse().getCp());
        adresseClientDTO.setVille(dossier.getClient().getAdresse().getVille());
        adresseClientDTO.setComplementAdresse(dossier.getClient().getAdresse().getComplementAdresse());
        
        dto.setAdresse(adresseClientDTO);

        
        return dto;
    }
}
