package fr.cdrochon.smamonolithe.document.command.services;

import fr.cdrochon.smamonolithe.document.command.commands.DocumentCreateCommand;
import fr.cdrochon.smamonolithe.document.command.dtos.DocumentCommandDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class DocumentCommandService {
    
    private final CommandGateway commandGateway;
    //utiliser une CompletableFuture pour synchroniser l'attente du contrôleur jusqu'à ce que l'événement soit reçu et traité
    private CompletableFuture<DocumentCommandDTO> futureDTO;
    
    public DocumentCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
    
    /**
     * Genere un UUID aleatoirement pour la creation d'un id de document
     *
     * @param documentRestDTO contenant les informations du document a creer
     * @return CompletableFuture that supports dependent functions and actions triggered upon its completion
     */
    @Transactional
    public CompletableFuture<DocumentCommandDTO> createDocument(DocumentCommandDTO documentRestDTO) {
        //CompletableFuture<DossierCommandDTO> sera complétée lorsque l'événement sera reçu.
        futureDTO = new CompletableFuture<>();
        
        commandGateway.send(new DocumentCreateCommand(UUID.randomUUID().toString(),
                                                      documentRestDTO.getNomDocument(),
                                                      documentRestDTO.getTitreDocument(),
                                                      documentRestDTO.getEmetteurDuDocument(),
                                                      documentRestDTO.getTypeDocument(),
                                                      documentRestDTO.getDateCreationDocument(),
                                                      documentRestDTO.getDateModificationDocument(),
                                                      documentRestDTO.getDocumentStatus()
        ));
        return futureDTO;
    }
    
    /**
     * Compléter la future dans le service. Méthode appelée par @EventHandler
     *
     * @param dto DTO de création d'un garage
     */
    public void completeDocumentCreation(DocumentCommandDTO dto) {
        if(futureDTO != null) {
            futureDTO.complete(dto);
        }
    }
    
}
