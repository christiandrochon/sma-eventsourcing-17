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
@ToString
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
    //    @OneToMany
    //    @JoinColumn(name = "dossier_id")
//    @OneToMany
//    @JoinTable(
//            name = "dossier",
////            joinColumns = @JoinColumn(name = "dossier_id"),
//            inverseJoinColumns = @JoinColumn(name = "vehicule_id")
//    )
    @OneToOne
    @JoinColumn(name = "vehicule_id")
    private Vehicule vehicule;
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
    
    
}
