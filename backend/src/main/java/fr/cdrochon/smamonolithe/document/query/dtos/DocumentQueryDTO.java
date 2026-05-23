package fr.cdrochon.smamonolithe.document.query.dtos;

import fr.cdrochon.smamonolithe.document.command.enums.DocumentStatusDTO;
import fr.cdrochon.smamonolithe.document.query.entities.TypeDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO représentant un document dans une requête de lecture")
public class DocumentQueryDTO {
    
    @Schema(description = "Identifiant unique du document", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    @Schema(description = "Nom du document", example = "facture_123.pdf")
    private String nomDocument;
    @Schema(description = "Titre du document", example = "Facture d'achat")
    private String titreDocument;
    @Schema(description = "Émetteur du document", example = "Garage ABC")
    private String emetteurDuDocument;
    @Schema(description = "Type de document")
    private TypeDocument typeDocument;
    @Schema(description = "Date de création du document")
    private Instant dateCreationDocument;
    @Schema(description = "Date de dernière modification du document")
    private Instant dateModificationDocument;
    @Schema(description = "Statut du document")
    private DocumentStatusDTO documentStatus;
}
