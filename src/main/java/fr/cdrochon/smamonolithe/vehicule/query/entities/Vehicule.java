package fr.cdrochon.smamonolithe.vehicule.query.entities;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import lombok.*;


import javax.persistence.*;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicule {
    @Id
    private String idVehicule;
    
    private String immatriculationVehicule;
    @Column(nullable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private Instant dateMiseEnCirculationVehicule;
    
    //    private Instant dateDeValiditeControleTechnique;
    //    private Instant dateValiditeControleTechniqueComplementaire;
    //    private String urlCertificatImmatriculation;
    //    private String modeleVehicule;
    //    private String versionVehicule;
    //    @Embedded private MarqueVehicule marqueVehicule;
    //    @Embedded private MotorisationVehicule motorisationVehicule;
    //    @Embedded private TypeCarburant typeCarburant;
    //    @Embedded private TypeBoiteVitesse typeBoiteVitesse;
    //    @Embedded private TypeDirectionAssistee typeDirectionAssistee;
    //    @Embedded private TypeFreinage typeFreinage;
    //    @Embedded private TypePropulsion typePropulsion;
    //    @Embedded private TypeSuspension typeSuspension;
    //    @Embedded private TypeVehicule typeVehicule;
    //    private String finitionMotorisationVehicule;
    //    private int puissanceFiscaleVehicule;
    //    private int puissanceVehicule;
    //    private int nombrePortesVehicule;
    //    private int nombrePlacesVehicule;
    //    private int kilometrageVehicule;
    //    private int anneeVehicule;
    //    private String couleurVehicule;
    //    private String urlPhotoVehicule;
    //    private boolean climatisationVehicule;
    @Enumerated(EnumType.STRING)
    private VehiculeStatus vehiculeStatus;
    
    //relation qui ne doit pas etre serialisée
    @OneToOne(mappedBy = "vehicule")
    @JsonBackReference("dossier-vehicule")
//    @JsonIgnore
    private Dossier dossier;
    
    /**
     * Au lieu d'inclure l'objet Dossier complet dans le toString(), on inlue uniquement l'identifiant du Dossier. Cela évite la récursion infinie tout en
     * fournissant une information utile sur la relation
     *
     * @return String représentant l'objet Vehicule
     */
    @Override
    public String toString() {
        return "Vehicule{" +
                "idVehicule=" + idVehicule +
                // Ne pas appeler dossier.toString() pour éviter la récursion
                ", dossierId=" + (dossier != null ? dossier.getId() : "null") +
                '}';
    }
}
