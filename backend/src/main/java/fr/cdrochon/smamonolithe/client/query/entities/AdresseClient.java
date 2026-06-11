package fr.cdrochon.smamonolithe.client.query.entities;


import fr.cdrochon.smamonolithe.client.query.dtos.ClientAdresseDTO;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import lombok.*;




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
