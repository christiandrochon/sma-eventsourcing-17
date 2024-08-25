package fr.cdrochon.smamonolithe.dossier.query.mapper;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import org.springframework.stereotype.Component;

@Component
public class DossierQueryMapper {
    
    public static VehiculeQueryDTO convertVehiculeToVehiculeDTO(Dossier dossier){
        if(dossier == null){
            return null;
        }
        VehiculeQueryDTO dto = new VehiculeQueryDTO();
        dto.setId(dossier.getVehicule().getId());
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
    
    public static DossierQueryDTO convertDossierToDossierDTO(Dossier dossier){
        if(dossier == null){
            return null;
        }
        //TODO tester ce que je recois pour voir si la conversion est necessaire !!
        VehiculeQueryDTO vehiculeDTO = convertVehiculeToVehiculeDTO(dossier);
//        vehiculeDTO.setId(dossier.getVehicule().getId());
//        vehiculeDTO.setImmatriculationVehicule(dossier.getVehicule().getImmatriculationVehicule());
//        vehiculeDTO.setDateMiseEnCirculationVehicule(dossier.getVehicule().getDateMiseEnCirculationVehicule());
//        vehiculeDTO.setVehiculeStatus(dossier.getVehicule().getVehiculeStatus());
        
        ClientQueryDTO clientDTO = convertClientToClientDTO(dossier);
//        clientDTO.setId(dossier.getClient().getId());
//        clientDTO.setNomClient(dossier.getClient().getNomClient());
//        clientDTO.setPrenomClient(dossier.getClient().getPrenomClient());
//        clientDTO.getMailClient(dossier.getClient().getMailClient());
//        clientDTO.setTelClient(dossier.getClient().getTelClient());
//        clientDTO.setAdresse(dossier.getClient().getAdresse());
        
        DossierQueryDTO dto = new DossierQueryDTO();
        dto.setId(dossier.getId());
        dto.setNomDossier(dossier.getNomDossier());
        dto.setDateCreationDossier(dossier.getDateCreationDossier());
        dto.setDateModificationDossier(dossier.getDateModificationDossier());
        dto.setClient(clientDTO);
        dto.setVehicule(vehiculeDTO);
        dto.setDossierStatus(dossier.getDossierStatus());
//        dto.setClient(dossier.getClient());
//        dto.setVehicule(dossier.getVehicule());
        // inutile convetir un enum
//        dto.setDossierStatus(DossierStatusDTO.valueOf(dossier.getDossierStatus().name()));
        
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
        dto.setMailClient(dossier.getClient().getMailClient());
        dto.setTelClient(dossier.getClient().getTelClient());
        dto.setClientStatus(dossier.getClient().getClientStatus());
        
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
