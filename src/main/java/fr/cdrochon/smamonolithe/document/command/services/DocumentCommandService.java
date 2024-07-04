package fr.cdrochon.smamonolithe.document.command.services;

import fr.cdrochon.smamonolithe.document.command.commands.DocumentCreateCommand;
import fr.cdrochon.smamonolithe.document.command.dtos.DocumentRestDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class DocumentCommandService {
    
    private final CommandGateway commandGateway;
    
    public DocumentCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }
    
    /**
     * Genere un UUID aleatoirement pour la creation d'un id de document
     *
     * @param documentRestDTO contenant les informations du document a creer
     * @return CompletableFuture that supports dependent functions and actions triggered upon its completion
     */
    public CompletableFuture<String> createDocument(DocumentRestDTO documentRestDTO) {
        System.out.println("DocumentCommandService.createDocument");
        return commandGateway.send(new DocumentCreateCommand(UUID.randomUUID().toString(),
                                                             documentRestDTO.getNomDocument(),
                                                             documentRestDTO.getTitreDocument(),
                                                             documentRestDTO.getEmetteurDuDocument(),
                                                             documentRestDTO.getTypeDocument(),
                                                             documentRestDTO.getDateCreationDocument(),
                                                             documentRestDTO.getDateModificationDocument()
        ));
    }
}
