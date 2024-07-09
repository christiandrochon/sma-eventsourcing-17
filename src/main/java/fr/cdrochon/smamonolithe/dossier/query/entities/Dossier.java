package fr.cdrochon.smamonolithe.dossier.query.entities;

import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.vehicule.query.entities.Vehicule;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dossier {
    @Id
    private String id;
    private String nomDossier;
    private Instant dateCreationDossier;
    private Instant dateModificationDossier;
    //    private Instant dateClotureDossier;
    @OneToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToOne
    @JoinColumn(name = "vehicule_id")
    private Vehicule vehicule;
    @Enumerated
    private DossierStatus dossierStatus;
    //    private String idConseiller;
    //    private String idAgence;
    //    private String idProduit;
    //    private String idTypeDossier;
    //    private String idStatutDossier;
    //    private String idMotifClotureDossier;
    //    private String idMotifReouvertureDossier;
    //    private String idMotifAnnulationDossier;
    //    private String idMotifRefusDossier;
    //    private String idMotifModificationDossier;
    //    private String idMotifValidationDossier;
    //    private String idMotifRejetDossier;
    //    private String idMotifAcceptationDossier;
    //    private String idMotifTraitementDossier;
    //    private String idMotifEnvoiDossier;
    //    private String idMotifReceptionDossier;
    //    private String idMotifRetourDossier;
    //    private String idMotifArchivageDossier;
    //    private String idMotifDesarchivageDossier;
    //    private String idMotifSuppressionDossier;
    //    private String idMotifRestaurationDossier;
    //    private String idMotifPurgeDossier;
    //    private String idMotifDepotDossier;
    //    private String idMotifRetraitDossier;
    //    private String idMotifValidationClientDossier;
    //    private String idMotifRefusClientDossier;
    //    private String idMotifModificationClientDossier;
    //    private String idMotifValidationConseillerDossier;
    //    private String idMotifRefusConseillerDossier;
    //    private String idMotifModificationConseillerDossier;
    //    private String idMotifValidationAgenceDossier;
    //    private String idMotifRefusAgenceDossier;
    //    private String idMotifModificationAgenceDossier;
    //    private String idMotifValidationProduitDossier;
    //    private String idMotifRefusProduitDossier;
    //    private String idMotifModificationProduitDossier;
    //    private String idMotifValidationTypeDossier;
    //    private String idMotifRefusTypeDossier;
    //    private String idMotifModificationTypeDossier;
    //    private String idMotifValidationStatutDossier;
    //    private String idMotifRefusStatutDossier;
    //    private String idMotifModificationStatutDossier;
    //    private String idMotifValidationMotifClotureDossier;
    //    private String idMotifRefusMotifClotureDossier;
    //    private String idMotifModificationMotifClotureDossier;
    //    private String idMotifValidationMotifReouvertureDossier;
    
    /**
     * Au lieu d'inclure l'objet Dossier complet dans le toString(), on inclut uniquement l'identifiant du Dossier. Cela évite la récursion infinie tout en
     * fournissant une information utile sur la relation
     *
     * @return String représentant l'objet Dossier
     */
    @Override
    public String toString() {
        return "Dossier{" +
                "id=" + id +
                ", client=" + (client != null ? "Client[id=" + client.getId() + "]" : "null") +
                ", vehicule=" + (vehicule != null ? "Vehicule[id=" + vehicule.getIdVehicule() + "]" : "null") +
                '}';
    }
}
