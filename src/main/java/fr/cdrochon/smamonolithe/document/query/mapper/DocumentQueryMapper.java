package fr.cdrochon.smamonolithe.document.query.mapper;

import fr.cdrochon.smamonolithe.document.query.dtos.DocumentQueryDTO;
import fr.cdrochon.smamonolithe.document.query.entities.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentQueryMapper {
    
    public static DocumentQueryDTO convertDocumentToDocumentDTO(Document document){
        if(document == null){
            return null;
        }
        DocumentQueryDTO dto = new DocumentQueryDTO();
        dto.setId(document.getId());
        dto.setNomDocument(document.getNomDocument());
        dto.setTitreDocument(document.getTitreDocument());
        dto.setEmetteurDuDocument(document.getEmetteurDuDocument());
        dto.setTypeDocument(document.getTypeDocument());
        dto.setDateCreationDocument(document.getDateCreationDocument());
        dto.setDateModificationDocument(document.getDateModificationDocument());
        dto.setDocumentStatus(document.getDocumentStatus());
        
        return dto;
    }
    
    public static Document convertDocumentDTOToDocument(DocumentQueryDTO documentDTO){
        if(documentDTO == null){
            return null;
        }
        Document document = new Document();
        document.setId(documentDTO.getId());
        document.setNomDocument(documentDTO.getNomDocument());
        document.setTitreDocument(documentDTO.getTitreDocument());
        document.setEmetteurDuDocument(documentDTO.getEmetteurDuDocument());
        document.setTypeDocument(documentDTO.getTypeDocument());
        document.setDateCreationDocument(documentDTO.getDateCreationDocument());
        document.setDateModificationDocument(documentDTO.getDateModificationDocument());
        document.setDocumentStatus(documentDTO.getDocumentStatus());
        
        return document;
    }
}
