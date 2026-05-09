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
     * Convertit une entité Dossier en DossierQueryDTO.
     *
     * @param dossier Dossier à convertir
     * @return DossierQueryDTO converti
     */
    public static DossierQueryDTO convertDossierToDossierDTO(Dossier dossier) {
        if(dossier == null) {
            return null;
        }
        
        DossierQueryDTO dto = new DossierQueryDTO();
        dto.setId(dossier.getId());
        dto.setNomDossier(dossier.getNomDossier());
        dto.setDateCreationDossier(dossier.getDateCreationDossier());
        dto.setDateModificationDossier(dossier.getDateModificationDossier());
        dto.setDossierStatus(dossier.getDossierStatus());
        
        dto.setClient(creeClientDepuisDossier(dossier));
        dto.setVehicule(creeVehiculeDtoDepuisDossier(dossier));
        
        return dto;
    }
    
    /**
     * Ajoute le contenu d'un dossier dans un VehiculeQueryDTO.
     *
     * @param dossier Dossier à ajouter
     * @return VehiculeQueryDTO contenant les informations du dossier
     */
    public static VehiculeQueryDTO creeVehiculeDtoDepuisDossier(Dossier dossier) {
        if(dossier == null) {
            return null;
        }
        VehiculeQueryDTO vehicule = new VehiculeQueryDTO();
        vehicule.setId(dossier.getVehicule().getId());
        vehicule.setImmatriculationVehicule(dossier.getVehicule().getImmatriculationVehicule());
        vehicule.setDateMiseEnCirculationVehicule(dossier.getVehicule().getDateMiseEnCirculationVehicule());
        vehicule.setVehiculeStatus(dossier.getVehicule().getVehiculeStatus());
        vehicule.setClient(null);
        
        
        return vehicule;
    }
    
    /**
     * Ajoute le contenu d'un dossier dans un ClientQueryDTO.
     *
     * @param dossier Dossier à ajouter
     * @return ClientQueryDTO contenant les informations du dossier
     */
    public static ClientQueryDTO creeClientDepuisDossier(Dossier dossier) {
        if(dossier == null) {
            return null;
        }
        
        ClientAdresseDTO adresseClientDTO = new ClientAdresseDTO();
        adresseClientDTO.setNumeroDeRue(dossier.getClient().getAdresse().getNumeroDeRue());
        adresseClientDTO.setRue(dossier.getClient().getAdresse().getRue());
        adresseClientDTO.setCp(dossier.getClient().getAdresse().getCp());
        adresseClientDTO.setVille(dossier.getClient().getAdresse().getVille());
        adresseClientDTO.setComplementAdresse(dossier.getClient().getAdresse().getComplementAdresse());
        
        ClientQueryDTO client = new ClientQueryDTO();
        client.setId(dossier.getClient().getId());
        client.setNomClient(dossier.getClient().getNomClient());
        client.setPrenomClient(dossier.getClient().getPrenomClient());
        client.setMailClient(dossier.getClient().getMailClient());
        client.setTelClient(dossier.getClient().getTelClient());
        client.setClientStatus(dossier.getClient().getClientStatus());
        client.setAdresse(adresseClientDTO);
        client.setVehicule(null);
        
        return client;
    }
}
