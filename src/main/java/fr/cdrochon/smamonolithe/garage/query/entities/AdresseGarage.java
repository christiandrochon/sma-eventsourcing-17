package fr.cdrochon.smamonolithe.garage.query.entities;

import fr.cdrochon.smamonolithe.garage.query.dto.GarageAdresseDTO;
import lombok.*;

import javax.persistence.Embeddable;


@Embeddable
@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class AdresseGarage {
    
    private String numeroDeRue;
    private String rue;
    private String cp;
    private String ville;
    
    /**
     * Copie de l'objet AdresseGarage pour éviter l'exposition de la représentation interne
     * @param adresseGarageDTO AdresseGarageDTO
     */
    public AdresseGarage(GarageAdresseDTO adresseGarageDTO) {
        this.numeroDeRue = adresseGarageDTO.getNumeroDeRue();
        this.rue = adresseGarageDTO.getRue();
        this.cp = adresseGarageDTO.getCp();
        this.ville = adresseGarageDTO.getVille();
    }
}
