package fr.cdrochon.smamonolithe.vehicule.command.commands;

import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

/**
 * Classe abstraite immutable qui effectue une command
 * Implemente l'ajout d'un vehicule via un event sur le CommandBus
 * <p>
 * Chaque command possede un id
 */
@Getter
public class VehiculeCreateCommand extends VehiculeBaseCommand<String> {
    private String immatriculationVehicule;
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
    
    private VehiculeStatus vehiculeStatus;
    
    public VehiculeCreateCommand(String id, String immatriculationVehicule, Instant dateMiseEnCirculationVehicule) {
        super(id);
        this.immatriculationVehicule = immatriculationVehicule;
        this.dateMiseEnCirculationVehicule = dateMiseEnCirculationVehicule;
    }
    
    /**
     * Capture n'importe quelle exception en interne et affiche son message
     *
     * @param exception
     * @return ResponseEntity<String>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
