package fr.cdrochon.thymeleaffrontend.dtos.document;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentConvertThymDTO {
    private String id;

    private String nomDocument;

    private String titreDocument;

    private String emetteurDuDocument;

    private TypeDocumentDTO typeDocument;

    private Instant dateCreationDocument;

    private Instant dateModificationDocument;

    private DocumentStatusDTO documentStatus;
}
