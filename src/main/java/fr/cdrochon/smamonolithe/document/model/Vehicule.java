package fr.cdrochon.smamonolithe.document.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;


@Getter
@Setter
@ToString
public class Vehicule {

    private Long id;
    private String immatriculationVehicule;
    private LocalDate dateMiseEnCirculationVehicule;
//    private LocalDate dateDeValiditeControleTechnique;
//    private LocalDate dateValiditeControleTechniqueComplementaire;
//    private String urlCertificatImmatriculation;
//    private String modeleVehicule;
//    private MotorisationVehicule motorisationVehicule;
//    private String finitionMotorisationVehicule;
//    private TypeCarburant typeCarburant;
//    private TypePropulsion typePropulsion;
    private TypeVehicule typeVehicule;
    private MarqueVehicule marqueVehicule;
//    private TypeFreinage typeFreinage;
//    private TypeSuspension typeSuspension;
    private boolean climatisationVehicule;
}
