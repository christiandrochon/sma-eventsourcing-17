package fr.cdrochon.smamonolithe.document.query.entities;


import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatus;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @Id
    private String id;
    private String nomDocument;
    private String titreDocument;
    private String emetteurDuDocument;
    @Embedded
    private TypeDocument typeDocument;
    @Column(nullable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private Instant dateCreationDocument;
    @Column(nullable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private Instant dateModificationDocument;
    @Enumerated
    private DocumentStatus documentStatus;
    //communication inter ms
//    @Transient
//    private Vehicule vehicule;
//    private Long vehiculeId;
}
