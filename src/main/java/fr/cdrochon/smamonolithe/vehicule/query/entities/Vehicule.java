package fr.cdrochon.smamonolithe.vehicule.query.entities;


import fr.cdrochon.smamonolithe.dossier.query.entities.Dossier;
import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import lombok.*;


import javax.persistence.*;
import java.time.Instant;

@Entity
@Getter
@Setter
@ToString
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
    
//    @ManyToOne
//    @JoinColumn(name = "dossier_id")
    @OneToOne(mappedBy = "vehicule")
    private Dossier dossier;

    // communication inter ms
//    @Transient
//    private Client client;
//    private String clientId;
}
