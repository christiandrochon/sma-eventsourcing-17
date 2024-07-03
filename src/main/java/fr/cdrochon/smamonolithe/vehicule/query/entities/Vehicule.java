package fr.cdrochon.smamonolithe.vehicule.query.entities;


import fr.cdrochon.smamonolithe.client.query.entities.Client;
import lombok.*;


import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String idVehicule;

    //FIXME : GOD CLASS
    private String immatriculationVehicule;
    private Instant dateMiseEnCirculationVehicule;
    private Instant dateDeValiditeControleTechnique;
    private Instant dateValiditeControleTechniqueComplementaire;
    private String urlCertificatImmatriculation;
    private String modeleVehicule;
    private String versionVehicule;
    @Embedded private MarqueVehicule marqueVehicule;
    @Embedded private MotorisationVehicule motorisationVehicule;
    @Embedded private TypeCarburant typeCarburant;
    @Embedded private TypeBoiteVitesse typeBoiteVitesse;
    @Embedded private TypeDirectionAssistee typeDirectionAssistee;
    @Embedded private TypeFreinage typeFreinage;
    @Embedded private TypePropulsion typePropulsion;
    @Embedded private TypeSuspension typeSuspension;
    @Embedded private TypeVehicule typeVehicule;
    private String finitionMotorisationVehicule;
    private int puissanceFiscaleVehicule;
    private int puissanceVehicule;
    private int nombrePortesVehicule;
    private int nombrePlacesVehicule;
    private int kilometrageVehicule;
    private int anneeVehicule;
    private String couleurVehicule;
    private String urlPhotoVehicule;
    private boolean climatisationVehicule;

    // communication inter ms
    @Transient
    private Client client;
    private String clientId;
}
