package fr.cdrochon.thymeleaffrontend.dtos.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentConvertPostDTO {
    private String id;

    private String nomDocument;

    private String titreDocument;

    private String emetteurDuDocument;

    private TypeDocumentDTO typeDocument;

    private Instant dateCreationDocument;

    private Instant dateModificationDocument;

    private DocumentStatusDTO documentStatus;
}
