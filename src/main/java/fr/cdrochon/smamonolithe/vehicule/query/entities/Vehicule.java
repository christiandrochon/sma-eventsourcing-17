//package fr.cdrochon.smamonolithe.vehicule.entity;
//
//import fr.cdrochon.smamonolithe.client.entity.Client;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//
//
//import java.time.LocalDate;
//
//@Entity
//@Getter
//@Setter
//@ToString
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Vehicule {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
//
//    //FIXME : GOD CLASS
//    private String immatriculationVehicule;
//    private LocalDate dateMiseEnCirculationVehicule;
////    private LocalDate dateDeValiditeControleTechnique;
////    private LocalDate dateValiditeControleTechniqueComplementaire;
////    private String urlCertificatImmatriculation;
////    private String modeleVehicule;
////    private MotorisationVehicule motorisationVehicule;
////    private String finitionMotorisationVehicule;
////    private TypeCarburant typeCarburant;
////    private TypePropulsion typePropulsion;
//    private TypeVehicule typeVehicule;
//    private MarqueVehicule marqueVehicule;
////    private TypeFreinage typeFreinage;
////    private TypeSuspension typeSuspension;
//    private boolean climatisationVehicule;
//
//    // communication inter ms
//    @Transient
//    private Client client;
//    private Long clientId;
//}
