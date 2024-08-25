package fr.cdrochon.smamonolithe.document.events;

import fr.cdrochon.smamonolithe.document.command.dtos.DocumentCommandDTO;
import fr.cdrochon.smamonolithe.document.command.services.DocumentCommandService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DocumentEventHandler {
    
    private final DocumentCommandService documentCommandService;
    
    public DocumentEventHandler(DocumentCommandService documentCommandService) {
        this.documentCommandService = documentCommandService;
    }
    
    /**
     * Complete le future dans le service DocumentCommandService lorsqu'un DocumentCreatedEvent est reçu
     *
     * @param event DocumentCreatedEvent event qui est declenché lors de la creation d'un document
     */
    @EventHandler
    public void on(DocumentCreatedEvent event) {
        
        log.info("Received event: {}", event);
        //conversion du dto du dossier en entité dossier
        DocumentCommandDTO commandDTO = new DocumentCommandDTO(event.getId(),
                                                               event.getNomDocument(),
                                                               event.getTitreDocument(),
                                                               event.getEmetteurDuDocument(),
                                                               event.getTypeDocument(),
                                                               event.getDateCreationDocument(),
                                                               event.getDateModificationDocument(),
                                                               event.getDocumentStatus());
        // Compléter la future dans le service
        documentCommandService.completeDocumentCreation(commandDTO);
    }
}
