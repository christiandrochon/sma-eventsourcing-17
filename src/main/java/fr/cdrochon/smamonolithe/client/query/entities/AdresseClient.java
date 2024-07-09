package fr.cdrochon.smamonolithe.client.query.entities;


import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.Enumerated;


@Embeddable
@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class AdresseClient {
    
    private String numeroDeRue;
    private String rue;
    private String complementAdresse;
    private String cp;
    private String ville;
    @Enumerated
    private Pays pays;
    
//    public AdresseClient(AdresseClient adresseClient) {
//        this.numeroDeRue = adresseClient.getNumeroDeRue();
//        this.rue = adresseClient.getRue();
//        this.cp = adresseClient.getCp();
//        this.ville = adresseClient.getVille();
//    }
    
    /**
     * Copie de l'objet AdresseClient pour éviter l'exposition de la représentation interne
     * @param adresseClient AdresseClient
     */
    public AdresseClient(ClientAdresseDTO adresseClient) {
        this.numeroDeRue = adresseClient.getNumeroDeRue();
        this.rue = adresseClient.getRue();
        this.cp = adresseClient.getCp();
        this.ville = adresseClient.getVille();
        this.pays = Pays.valueOf(adresseClient.getPays().name());
    }
}
