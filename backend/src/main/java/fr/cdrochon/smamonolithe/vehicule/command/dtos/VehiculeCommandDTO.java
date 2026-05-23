package fr.cdrochon.smamonolithe.vehicule.command.dtos;

import fr.cdrochon.smamonolithe.vehicule.command.enums.VehiculeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "DTO utilisé pour les commandes liées aux véhicules")
public class VehiculeCommandDTO {

    @Schema(description = "Identifiant unique du véhicule", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    @Schema(description = "Immatriculation du véhicule", example = "AB-123-CD")
    private String immatriculationVehicule;
    @Schema(description = "Date de mise en circulation du véhicule")
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
    
    @Schema(description = "Statut du véhicule (ACTIVE, INACTIVE)", example = "ACTIVE")
    private VehiculeStatus vehiculeStatus;
}
