package fr.cdrochon.smamonolithe.vehicule.event;

import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import lombok.Getter;

import java.time.Instant;

@Getter
public class VehiculeCreatedEvent extends VehiculeBaseEvent<String>{
    
    private final String immatriculationVehicule;
    private final Instant dateMiseEnCirculationVehicule;
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
    
    private final VehiculeStatus vehiculeStatus;

    
    public VehiculeCreatedEvent(String id, String immatriculationVehicule, Instant dateMiseEnCirculationVehicule, VehiculeStatus vehiculeStatus) {
        super(id);
        this.immatriculationVehicule = immatriculationVehicule;
        this.dateMiseEnCirculationVehicule = dateMiseEnCirculationVehicule;
        this.vehiculeStatus = vehiculeStatus;
    }
}
