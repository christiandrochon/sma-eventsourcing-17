package fr.cdrochon.thymeleaffrontend.dtos.vehicule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculePostDTO {
    
    private String idVehicule;
    @NotBlank(message = "L'immatriculation du véhicule est obligatoire, merci de la renseigner.")
    @Pattern(regexp = "^[A-Z]{2}-\\d{3}-[A-Z]{2}$", message = "Le format requis doit etre de type AA-123-AA")
    private String immatriculationVehicule;
    @NotBlank(message = "La date de mise en circulation est requise, merci de la renseigner.")
//    @DateTimeFormat(pattern = "yyyy-MM-dd")
//    @PastOrPresent(message = "la date de mise en circulation doit être passée ou présente")
    private String dateMiseEnCirculationVehicule;
    
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
    @NotNull(message = "Le statut du véhicule est obligatoire, merci de le renseigner.")
    private VehiculeStatusDTO vehiculeStatus;

}
