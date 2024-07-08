//package fr.cdrochon.thymeleaffrontend.entity.client;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Getter
//@Setter
//@ToString
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Client {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private String id;
//    private String nomClient;
//    private String prenomClient;
//    private String mailClient;
//    private String telClient;
//    @Embedded
//    private AdresseClient adresse;
//    @Enumerated(EnumType.STRING)
//    private ClientStatus clientStatus;
//}
