package fr.cdrochon.smamonolithe.document.query.services;

import fr.cdrochon.smamonolithe.document.events.DocumentCreatedEvent;
import fr.cdrochon.smamonolithe.document.query.dtos.DocumentQueryDTO;
import fr.cdrochon.smamonolithe.document.query.dtos.GetDocumentDTO;
import fr.cdrochon.smamonolithe.document.query.entities.Document;
import fr.cdrochon.smamonolithe.document.query.mapper.DocumentQueryMapper;
import fr.cdrochon.smamonolithe.document.query.repositories.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hibernate.TransactionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocumentEventHandlerService {
    
    private final DocumentRepository documentRepository;
    
    public DocumentEventHandlerService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }
    
    /**
     * subscribe sur le service DocumentQueryCreatedEvent
     *
     * @param event DocumentCreatedEvent event qui est declenché lors de la creation d'un document
     */
    @EventHandler
    @Transactional
    public void on(DocumentCreatedEvent event) {
        
        log.info("********************************");
        log.info("SAUVEGARDE DU DOCUMENT");
        try {
            Document document = new Document();
            document.setId(event.getId());
            document.setNomDocument(event.getNomDocument());
            document.setTitreDocument(event.getTitreDocument());
            document.setEmetteurDuDocument(event.getEmetteurDuDocument());
            document.setTypeDocument(event.getTypeDocument());
            document.setDateCreationDocument(event.getDateCreationDocument());
            document.setDateModificationDocument(event.getDateModificationDocument());
            document.setDocumentStatus(event.getDocumentStatus());
            
            documentRepository.save(document);
            
        } catch(Exception e) {
            System.out.println("TRANSACTION DOCUMENT ERREUR : " + e.getMessage());
            throw new TransactionException("Erreur lors de la sauvegarde du document");
        }
    }
    
    /**
     * Recupere et renvoi un document avec son id
     *
     * @param getDocumentDTO contenant l'id du document
     * @return DocumentResponseDTO contenant les informations du document
     */
    @QueryHandler
    public DocumentQueryDTO on(GetDocumentDTO getDocumentDTO) {
        return documentRepository.findById(getDocumentDTO.getId())
                                 .map(DocumentQueryMapper::convertDocumentToDocumentDTO)
                                 .orElseThrow(() -> new EntityNotFoundException("Document not found"));
    }
    
    /**
     * Recupere et renvoi tous les documents
     *
     * @return List<DocumentResponseDTO> contenant les informations de tous les documents
     */
    @QueryHandler
    public List<DocumentQueryDTO> on() {
        List<Document> documents = documentRepository.findAll();
        return documents.stream().map(DocumentQueryMapper::convertDocumentToDocumentDTO).collect(Collectors.toList());
    }
}
