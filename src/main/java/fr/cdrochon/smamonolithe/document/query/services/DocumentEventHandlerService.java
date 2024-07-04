package fr.cdrochon.smamonolithe.document.query.services;

import fr.cdrochon.smamonolithe.client.events.ClientCreatedEvent;
import fr.cdrochon.smamonolithe.document.events.DocumentCreatedEvent;
import fr.cdrochon.smamonolithe.document.query.dtos.DocumentResponseDTO;
import fr.cdrochon.smamonolithe.document.query.dtos.GetDocumentDTO;
import fr.cdrochon.smamonolithe.document.query.entities.Document;
import fr.cdrochon.smamonolithe.document.query.mapper.DocumentMapper;
import fr.cdrochon.smamonolithe.document.query.repositories.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class DocumentEventHandlerService {
    
    private final DocumentRepository documentRepository;
    private final QueryUpdateEmitter queryUpdateEmitter;
    
    public DocumentEventHandlerService(DocumentRepository documentRepository, QueryUpdateEmitter queryUpdateEmitter) {
        this.documentRepository = documentRepository;
        this.queryUpdateEmitter = queryUpdateEmitter;
    }
    
    /**
     * On fait un subscribe avec @EventHandler = j'ecoute ce que fait le service DocumentCreatedEvent
     * <p>
     * EventMessage sert à recuperer toutes les informations sur l'event avec la methode payLoad(). Il est donc plus general que l'event créé par moi-meme
     *
     * @param event l'event GarageQueryCreatedEvent
     */
    @EventHandler
    public void on(DocumentCreatedEvent event, EventMessage<ClientCreatedEvent> eventMessage) {
        log.info("********************************");
        log.info("DocumentCreatedEvent received !!!!!!!!!!!!!!!!!!!!!!");
        log.info("Identifiant d'evenement : " + event.getId());
        log.info("Identifiant d'agregat : " + eventMessage.getIdentifier());
        
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
            System.out.println("EXTRACT DOCUMENT ERREUR : " + e.getMessage());
        }
    }
    
    /**
     * Recupere et renvoi un document avec son id
     *
     * @param getDocumentDTO contenant l'id du document
     * @return DocumentResponseDTO contenant les informations du document
     */
    @QueryHandler
    public DocumentResponseDTO on(GetDocumentDTO getDocumentDTO) {
        return documentRepository.findById(getDocumentDTO.getId()).map(DocumentMapper::convertDocumentToDocumentDTO).get();
    }
    
    /**
     * Recupere et renvoi tous les documents
     *
     * @return List<DocumentResponseDTO> contenant les informations de tous les documents
     */
    @QueryHandler
    public List<DocumentResponseDTO> on() {
        List<Document> documents = documentRepository.findAll();
        return documents.stream().map(DocumentMapper::convertDocumentToDocumentDTO).collect(Collectors.toList());
    }
}
