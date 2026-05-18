package fr.cdrochon.smamonolithe.document.query.entities;


import fr.cdrochon.smamonolithe.client.query.entities.Client;
import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import lombok.*;

import jakarta.persistence.*;
import java.time.Instant;

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
    //    @ManyToOne
    //    @JoinColumn(name = "typeDocument_id")
//    @JsonDeserialize(using = TypeDocumentDeserializer.class)
    @Embedded
    private TypeDocument typeDocument;
    @Column(nullable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private Instant dateCreationDocument;
    @Column(nullable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private Instant dateModificationDocument;
    @Enumerated
    private DocumentStatusDTO documentStatus;

    // Lien vers le propriétaire du document – utilisé pour le filtrage RBAC
    // Nullable : rétrocompatibilité avec les documents créés avant V3
    @ManyToOne(fetch = FetchType.LAZY)
    private Client client;

    public Document(String id,
                    String nomDocument,
                    String titreDocument,
                    String emetteurDuDocument,
                    TypeDocument typeDocument,
                    Instant dateCreationDocument,
                    Instant dateModificationDocument,
                    DocumentStatusDTO documentStatus) {
        this(id, nomDocument, titreDocument, emetteurDuDocument, typeDocument,
                dateCreationDocument, dateModificationDocument, documentStatus, null);
    }
}
