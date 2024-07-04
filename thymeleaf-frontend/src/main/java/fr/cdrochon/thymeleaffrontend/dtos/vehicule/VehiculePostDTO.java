package fr.cdrochon.thymeleaffrontend.dtos.vehicule;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculePostDTO {
    
    private String idVehicule;
    @NotBlank(message = "champ obligatoire")
//    @Size(min = 1, max = 10, message = "l'immatriculation doit contenir entre 1 et 10 caractères")
    private String immatriculationVehicule;
    @NotBlank(message = "champ obligatoire")
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
    

}
