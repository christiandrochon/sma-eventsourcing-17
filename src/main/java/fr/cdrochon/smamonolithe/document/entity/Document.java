//package fr.cdrochon.smamonolithe.document.entity;
//
//import fr.cdrochon.smamonolithe.vehicule.entity.Vehicule;
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
//@Builder
//@ToString
//@NoArgsConstructor
//@AllArgsConstructor
//public class Document {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
//    private String nomDocument;
//    private String titreDocument;
//    @Enumerated(EnumType.STRING)
//    private TypeDocument typeDocument;
//    private String emetteurDuDocument;
//    private LocalDate dateCreationDocument;
//    private LocalDate dateModificationDocument;
//    //communication inter ms
//    @Transient
//    private Vehicule vehicule;
//    private Long vehiculeId;
//}
