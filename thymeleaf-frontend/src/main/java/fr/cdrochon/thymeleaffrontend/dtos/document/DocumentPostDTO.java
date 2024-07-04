package fr.cdrochon.thymeleaffrontend.dtos.document;

import fr.cdrochon.thymeleaffrontend.entity.document.DocumentStatus;
import fr.cdrochon.thymeleaffrontend.entity.document.TypeDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentPostDTO {
    
    private String id;
    @NotBlank(message = "champ obligatoire")
    @Size(min = 3, max = 50)
    private String nomDocument;
    @NotBlank(message = "champ obligatoire")
    @Size(min = 3, max = 50)
    private String titreDocument;
    @NotBlank(message = "champ obligatoire")
    @Size(min = 3, max = 50)
    private String emetteurDuDocument;
    private TypeDocument typeDocument;
    private Instant dateCreationDocument;
    private Instant dateModificationDocument;
    private DocumentStatus documentStatus;
}
