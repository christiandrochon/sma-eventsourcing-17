package fr.cdrochon.smamonolithe.document.query.mapper;

import fr.cdrochon.smamonolithe.document.query.dtos.DocumentResponseDTO;
import fr.cdrochon.smamonolithe.document.query.entities.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {
    
    public static DocumentResponseDTO convertDocumentToDocumentDTO(Document document){
        DocumentResponseDTO dto = new DocumentResponseDTO();
        dto.setIdDocument(document.getId());
        dto.setNomDocument(document.getNomDocument());
        dto.setTitreDocument(document.getTitreDocument());
        dto.setEmetteurDuDocument(document.getEmetteurDuDocument());
        dto.setTypeDocument(document.getTypeDocument());
        dto.setDateCreationDocument(document.getDateCreationDocument());
        dto.setDateModificationDocument(document.getDateModificationDocument());
        dto.setDocumentStatus(document.getDocumentStatus());
        
        return dto;
    }
}
