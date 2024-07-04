package fr.cdrochon.thymeleaffrontend.entity.document;

import fr.cdrochon.thymeleaffrontend.entity.vehicule.Vehicule;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String nomDocument;
    private String titreDocument;
    @Enumerated(EnumType.STRING)
    private TypeDocument typeDocument;
    private String emetteurDuDocument;
    private LocalDate dateCreationDocument;
    private LocalDate dateModificationDocument;
    //communication inter ms
//    @Transient
//    private Vehicule vehicule;
//    private Long vehiculeId;
}
