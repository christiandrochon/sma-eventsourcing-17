package fr.cdrochon.smamonolithe.dossier.query.mapper;

import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import fr.cdrochon.smamonolithe.client.query.dtos.ClientQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.dtos.DossierQueryDTO;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.vehicule.query.dtos.VehiculeQueryDTO;
import org.springframework.stereotype.Component;

@Component
public class DossierQueryMapper {
    
    /**
     * Ajoute le contenu d'un dossier dans un VehiculeQueryDTO.
     *
     * @param dossier Dossier à ajouter
     * @return VehiculeQueryDTO contenant les informations du dossier
     */
    public static VehiculeQueryDTO convertVehiculeToVehiculeDTO(Dossier dossier) {
        if(dossier == null) {
            return null;
        }
        VehiculeQueryDTO dto = new VehiculeQueryDTO();
        dto.setId(dossier.getVehicule().getId());
        dto.setImmatriculationVehicule(dossier.getVehicule().getImmatriculationVehicule());
        dto.setDateMiseEnCirculationVehicule(dossier.getVehicule().getDateMiseEnCirculationVehicule());
        dto.setVehiculeStatus(dossier.getVehicule().getVehiculeStatus());
        dto.setClient(convertClientToClientDTO(dossier));
        
        return dto;
    }
    
    /**
     * Ajoute le contenu d'un dossier dans un ClientQueryDTO.
     *
     * @param dossier Dossier à ajouter
     * @return ClientQueryDTO contenant les informations du dossier
     */
    public static ClientQueryDTO convertClientToClientDTO(Dossier dossier) {
        if(dossier == null) {
            return null;
        }
        ClientQueryDTO dto = new ClientQueryDTO();
        dto.setId(dossier.getClient().getId());
        dto.setNomClient(dossier.getClient().getNomClient());
        dto.setPrenomClient(dossier.getClient().getPrenomClient());
        dto.setMailClient(dossier.getClient().getMailClient());
        dto.setTelClient(dossier.getClient().getTelClient());
        dto.setClientStatus(dossier.getClient().getClientStatus());
        dto.setVehicule(convertVehiculeToVehiculeDTO(dossier));
        
        ClientAdresseDTO adresseClientDTO = new ClientAdresseDTO();
        adresseClientDTO.setNumeroDeRue(dossier.getClient().getAdresse().getNumeroDeRue());
        adresseClientDTO.setRue(dossier.getClient().getAdresse().getRue());
        adresseClientDTO.setCp(dossier.getClient().getAdresse().getCp());
        adresseClientDTO.setVille(dossier.getClient().getAdresse().getVille());
        adresseClientDTO.setComplementAdresse(dossier.getClient().getAdresse().getComplementAdresse());
        
        dto.setAdresse(adresseClientDTO);
        
        return dto;
    }
    
    /**
     * Convertit une entité Dossier en DossierQueryDTO.
     *
     * @param dossier Dossier à convertir
     * @return DossierQueryDTO converti
     */
    public static DossierQueryDTO convertDossierToDossierDTO(Dossier dossier) {
        if(dossier == null) {
            return null;
        }
        
        VehiculeQueryDTO vehiculeDTO = convertVehiculeToVehiculeDTO(dossier);
        
        ClientQueryDTO clientDTO = convertClientToClientDTO(dossier);
        
        DossierQueryDTO dto = new DossierQueryDTO();
        dto.setId(dossier.getId());
        dto.setNomDossier(dossier.getNomDossier());
        dto.setDateCreationDossier(dossier.getDateCreationDossier());
        dto.setDateModificationDossier(dossier.getDateModificationDossier());
        dto.setClient(clientDTO);
        dto.setVehicule(vehiculeDTO);
        dto.setDossierStatus(dossier.getDossierStatus());
        
        return dto;
    }
}
