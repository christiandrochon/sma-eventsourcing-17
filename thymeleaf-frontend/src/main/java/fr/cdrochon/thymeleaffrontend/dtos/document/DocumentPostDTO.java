package fr.cdrochon.thymeleaffrontend.dtos.document;

import fr.cdrochon.thymeleaffrontend.entity.document.DocumentStatus;
import fr.cdrochon.thymeleaffrontend.entity.document.TypeDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentPostDTO {
    
    private String id;
    @NotBlank(message = "Le nom du document est obligatoire")
    @Size(min = 3, max = 50)
    private String nomDocument;
    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 3, max = 50)
    private String titreDocument;
    @NotBlank(message = "L'emetteur du document doit etre renseigné")
    @Size(min = 3, max = 50)
    private String emetteurDuDocument;
    @NotNull(message = "Le type du document est obligatoire")
    private TypeDocument typeDocument;
    @NotBlank(message = "La date de création du document est obligatoire")
    private String dateCreationDocument;
    @NotBlank(message = "La date de modification du document est obligatoire")
    private String dateModificationDocument;
    @NotNull(message = "Le status du document est obligatoire")
    private DocumentStatus documentStatus;
}
