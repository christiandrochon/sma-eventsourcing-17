package fr.cdrochon.thymeleaffrontend.controller.dossier;

import fr.cdrochon.thymeleaffrontend.dtos.client.ClientThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierThymConvertDTO;
import fr.cdrochon.thymeleaffrontend.dtos.dossier.DossierThymDTO;
import fr.cdrochon.thymeleaffrontend.dtos.vehicule.VehiculeThymDTO;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DossierThymDtoMapper {
    
    /**
     * Convertion de DossierThymConvertDTO en DossierThymDTO
     *
     * @param dossierConvertDTO DossierThymConvertDTO
     * @return DossierThymDTO
     */
    private DossierThymDTO convertToDossierThymDto(DossierThymConvertDTO dossierConvertDTO) {
        
        VehiculeThymDTO vehiculeThymDTO = new VehiculeThymDTO();
        vehiculeThymDTO.setIdVehicule(dossierConvertDTO.getVehicule().getIdVehicule());
        vehiculeThymDTO.setImmatriculationVehicule(dossierConvertDTO.getVehicule().getImmatriculationVehicule());
        vehiculeThymDTO.setVehiculeStatus(dossierConvertDTO.getVehicule().getVehiculeStatus());
        //date vehicule date de mise en circulation
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        Instant dateMiseEnCirculationVehicule = dossierConvertDTO.getVehicule().getDateMiseEnCirculationVehicule();
        String localDate = dateMiseEnCirculationVehicule.atOffset(ZoneOffset.UTC).toLocalDate().format(formatter);
        vehiculeThymDTO.setDateMiseEnCirculationVehicule(localDate);
        
        ClientThymDTO clientThymDTO = new ClientThymDTO();
        clientThymDTO.setId(dossierConvertDTO.getClient().getId());
        clientThymDTO.setNomClient(dossierConvertDTO.getClient().getNomClient());
        clientThymDTO.setPrenomClient(dossierConvertDTO.getClient().getPrenomClient());
        clientThymDTO.setMailClient(dossierConvertDTO.getClient().getMailClient());
        clientThymDTO.setTelClient(dossierConvertDTO.getClient().getTelClient());
        clientThymDTO.setAdresse(dossierConvertDTO.getClient().getAdresse());
        clientThymDTO.setClientStatus(dossierConvertDTO.getClient().getClientStatus());
        
        
        DossierThymDTO dossierThymDTO = new DossierThymDTO();
        dossierThymDTO.setId(dossierConvertDTO.getId());
        dossierThymDTO.setNomDossier(dossierConvertDTO.getNomDossier());
        
        //date de creation du dossier
        Instant dateCreationDossier = dossierConvertDTO.getDateCreationDossier();
        String dateCreationDossierLocale = dateCreationDossier.atOffset(ZoneOffset.UTC).toLocalDate().format(formatter);
        dossierThymDTO.setDateCreationDossier(dateCreationDossierLocale);
        
        //dossier date de modification du dossier
        String dateModificationDossier = dossierConvertDTO.getDateModificationDossier().atOffset(ZoneOffset.UTC).toLocalDate().format(formatter);
        dossierThymDTO.setDateModificationDossier(dateModificationDossier);
        dossierThymDTO.setDossierStatus(dossierConvertDTO.getDossierStatus());
        dossierThymDTO.setClient(clientThymDTO);
        dossierThymDTO.setVehicule(vehiculeThymDTO);
        return dossierThymDTO;
    }
    
}
