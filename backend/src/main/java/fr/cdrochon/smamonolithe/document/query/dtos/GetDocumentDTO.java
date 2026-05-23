package fr.cdrochon.smamonolithe.document.query.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO pour récupérer un document par ID")
public class GetDocumentDTO {
    
    @Schema(description = "Identifiant du document à récupérer", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
}
